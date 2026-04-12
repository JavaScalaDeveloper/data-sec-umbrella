package com.arelore.data.sec.umbrella.server.worker.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineDatabaseScanDispatchPayload;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflinePolicySnapshot;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineScanSensitivitySnapshotMessage;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineScanSnapshotUniqueKey;
import com.arelore.data.sec.umbrella.server.core.dto.llm.AiRuleResult;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyAssetSample;
import com.arelore.data.sec.umbrella.server.core.service.checker.RulesChecker;
import com.arelore.data.sec.umbrella.server.worker.mq.OfflineScanSnapshotPublisher;
import com.arelore.data.sec.umbrella.server.worker.scanner.AssetScanResult;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI 扫描公共步骤（解析样例、调用检查器、发布快照），供各引擎 Handler 复用。
 */
public final class OfflineAiScanSupport {

    private OfflineAiScanSupport() {
    }

    /**
     * 与 {@link com.arelore.data.sec.umbrella.server.worker.scanner.AssetScanner#databaseType()} 及 MQ 资产字段对齐。
     */
    public static String resolveDatabaseType(Map<String, Object> asset, OfflineDatabaseScanDispatchPayload payload) {
        if (asset != null) {
            Object dt = asset.get("databaseType");
            if (dt != null && StringUtils.hasText(String.valueOf(dt).trim())) {
                return normalizeDatabaseType(String.valueOf(dt).trim());
            }
        }
        if (payload != null && StringUtils.hasText(payload.getEngine())) {
            return normalizeDatabaseType(payload.getEngine().trim());
        }
        return "MySQL";
    }

    public static String normalizeDatabaseType(String raw) {
        if (raw.equalsIgnoreCase("ClickHouse") || raw.equalsIgnoreCase("Clickhouse")) {
            return "Clickhouse";
        }
        if (raw.equalsIgnoreCase("MySQL")) {
            return "MySQL";
        }
        return raw;
    }

    public static Long toAssetId(Map<String, Object> asset) {
        if (asset == null) {
            return null;
        }
        Long id = toLong(asset.get("assetId"));
        if (id == null) {
            id = toLong(asset.get("id"));
        }
        return id;
    }

    public static Long toLong(Object v) {
        if (v == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception ex) {
            return null;
        }
    }

    public static String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public static List<DatabasePolicyAssetSample> parseSamples(Object value) {
        if (value == null) {
            return List.of();
        }
        try {
            return JSON.parseObject(JSON.toJSONString(value), new TypeReference<List<DatabasePolicyAssetSample>>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }

    public static AiScanResult evaluateAi(
            RulesChecker rulesChecker,
            List<DatabasePolicyAssetSample> samples,
            List<OfflinePolicySnapshot> policies
    ) {
        int maxLevel = 0;
        Set<String> tags = new LinkedHashSet<>();
        List<AssetScanResult.ColumnScanInfoItem> columnInfo = new ArrayList<>();
        if (samples == null || samples.isEmpty() || policies == null || policies.isEmpty()) {
            return new AiScanResult(maxLevel, tags, columnInfo, false);
        }
        boolean anyLlmInvocationFailed = false;
        for (DatabasePolicyAssetSample sample : samples) {
            int columnLevel = 0;
            Set<String> columnTags = new LinkedHashSet<>();
            List<String> sensitiveSamples = new ArrayList<>();
            for (OfflinePolicySnapshot p : policies) {
                if (!StringUtils.hasText(p.getAiRule())) {
                    continue;
                }
                AiRuleResult result = rulesChecker.checkAiRules(p.getAiRule(), List.of(sample));
                if (result != null && result.invocationFailed()) {
                    anyLlmInvocationFailed = true;
                }
                if (result != null && result.passed()) {
                    if (p.getSensitivityLevel() != null) {
                        columnLevel = Math.max(columnLevel, p.getSensitivityLevel());
                        maxLevel = Math.max(maxLevel, p.getSensitivityLevel());
                    }
                    if (StringUtils.hasText(p.getPolicyCode())) {
                        columnTags.add(p.getPolicyCode());
                        tags.add(p.getPolicyCode());
                    }
                    if (sample.getColumnValues() != null) {
                        sensitiveSamples.addAll(sample.getColumnValues());
                    }
                }
            }
            columnInfo.add(new AssetScanResult.ColumnScanInfoItem(
                    sample.getColumnName(),
                    String.valueOf(columnLevel),
                    new ArrayList<>(columnTags),
                    sensitiveSamples.stream().distinct().toList(),
                    sample.getColumnValues() == null ? List.of() : sample.getColumnValues()
            ));
        }
        return new AiScanResult(maxLevel, tags, columnInfo, anyLlmInvocationFailed);
    }

    public static void publishAiScanSnapshot(
            OfflineDatabaseScanDispatchPayload payload,
            String engine,
            String dataInstance,
            String databaseName,
            String tableName,
            AiScanResult aiResult,
            OfflineScanSnapshotPublisher publisher
    ) {
        if (!StringUtils.hasText(engine)) {
            engine = "MySQL";
        }
        String tableKey = OfflineScanSnapshotUniqueKey.tableRowKey(dataInstance, databaseName, tableName);
        OfflineScanSensitivitySnapshotMessage tableMsg = sensitivityRow(
                payload,
                "AI",
                engine,
                tableKey,
                String.valueOf(aiResult.maxLevel()),
                new ArrayList<>(aiResult.tags())
        );
        tableMsg.setColumnDetails(buildColumnDetailsJson(aiResult.columnInfo()));
        publisher.publishTableSnapshot(tableMsg);
        List<AssetScanResult.ColumnScanInfoItem> columns = aiResult.columnInfo();
        if (columns == null || columns.isEmpty()) {
            return;
        }
        for (AssetScanResult.ColumnScanInfoItem col : columns) {
            if (col == null || !StringUtils.hasText(col.columnName())) {
                continue;
            }
            String colKey = OfflineScanSnapshotUniqueKey.columnRowKey(dataInstance, databaseName, tableName, col.columnName());
            List<String> colTags = col.sensitivityTags() == null ? List.of() : col.sensitivityTags();
            String colLevel = col.sensitivityLevel() == null ? "0" : col.sensitivityLevel();
            OfflineScanSensitivitySnapshotMessage colMsg = sensitivityRow(payload, "AI", engine, colKey, colLevel, colTags);
            colMsg.setSamples(col.samples() == null ? List.of() : col.samples());
            colMsg.setSensitiveSamples(col.sensitiveSamples() == null ? List.of() : col.sensitiveSamples());
            publisher.publishColumnSnapshot(colMsg);
        }
    }

    private static String buildColumnDetailsJson(List<AssetScanResult.ColumnScanInfoItem> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        List<Map<String, Object>> list = new ArrayList<>(items.size());
        for (AssetScanResult.ColumnScanInfoItem i : items) {
            if (i == null) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("column_name", i.columnName());
            m.put("samples", i.samples() == null ? List.of() : i.samples());
            m.put("sensitive_samples", i.sensitiveSamples() == null ? List.of() : i.sensitiveSamples());
            m.put("sensitivity_level", i.sensitivityLevel());
            m.put("sensitivity_tags", i.sensitivityTags() == null ? List.of() : i.sensitivityTags());
            list.add(m);
        }
        return JSON.toJSONString(list);
    }

    private static OfflineScanSensitivitySnapshotMessage sensitivityRow(
            OfflineDatabaseScanDispatchPayload payload,
            String scanKind,
            String engine,
            String uniqueKey,
            String sensitivityLevel,
            List<String> sensitivityTags) {
        OfflineScanSensitivitySnapshotMessage m = new OfflineScanSensitivitySnapshotMessage();
        m.setInstanceId(payload.getInstanceId());
        m.setJobId(payload.getJobId());
        m.setDispatchVersion(payload.getDispatchVersion());
        m.setTaskName(payload.getTaskName());
        m.setScanKind(scanKind);
        m.setEngine(engine);
        m.setEventTime(System.currentTimeMillis());
        m.setUniqueKey(uniqueKey);
        m.setSensitivityLevel(sensitivityLevel);
        m.setSensitivityTags(sensitivityTags);
        return m;
    }

    public record AiScanResult(int maxLevel, Set<String> tags, List<AssetScanResult.ColumnScanInfoItem> columnInfo,
                               boolean llmInvocationFailed) {
    }
}
