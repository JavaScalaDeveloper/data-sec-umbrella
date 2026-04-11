package com.arelore.data.sec.umbrella.server.worker.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineDatabaseScanDispatchPayload;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflinePolicySnapshot;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineScanSensitivitySnapshotMessage;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineScanSnapshotUniqueKey;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyTestRulesRequest;
import com.arelore.data.sec.umbrella.server.core.entity.DbAssetMysqlScanOfflineJobInstance;
import com.arelore.data.sec.umbrella.server.core.entity.MySQLTableInfo;
import com.arelore.data.sec.umbrella.server.core.enums.OfflineJobRunStatusEnum;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetMysqlScanOfflineJobInstanceService;
import com.arelore.data.sec.umbrella.server.core.service.MySQLTableInfoService;
import com.arelore.data.sec.umbrella.server.core.service.llm.AiRuleLlmService;
import com.arelore.data.sec.umbrella.server.worker.mq.OfflineScanSnapshotPublisher;
import com.arelore.data.sec.umbrella.server.worker.scanner.AssetScanResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI 专用 Worker：消费 AI 队列，执行 AI 规则检测并回写资产。
 *
 * @author 黄佳豪
 */
@Service("offlineAiScanTaskProcessor")
public class AiTaskWorkerImpl implements OfflineScanTaskProcessor {

    private final AiRuleLlmService aiRuleLlmService;
    private final MySQLTableInfoService mySQLTableInfoService;
    private final StringRedisTemplate stringRedisTemplate;
    private final DbAssetMysqlScanOfflineJobInstanceService jobInstanceService;
    private final OfflineScanSnapshotPublisher offlineScanSnapshotPublisher;

    public AiTaskWorkerImpl(AiRuleLlmService aiRuleLlmService,
                            MySQLTableInfoService mySQLTableInfoService,
                            StringRedisTemplate stringRedisTemplate,
                            DbAssetMysqlScanOfflineJobInstanceService jobInstanceService,
                            OfflineScanSnapshotPublisher offlineScanSnapshotPublisher) {
        this.aiRuleLlmService = aiRuleLlmService;
        this.mySQLTableInfoService = mySQLTableInfoService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.jobInstanceService = jobInstanceService;
        this.offlineScanSnapshotPublisher = offlineScanSnapshotPublisher;
    }

    @Override
    public void process(OfflineDatabaseScanDispatchPayload payload) {
        if (payload == null || payload.getAssets() == null || payload.getAssets().isEmpty()) {
            return;
        }
        Long instanceId = payload.getInstanceId();
        for (Map<String, Object> asset : payload.getAssets()) {
            try {
                processOne(payload, asset);
                incrementRedis(instanceId, "ai_success");
            } catch (Exception ex) {
                incrementRedis(instanceId, "ai_fail");
            }
        }
        syncAiInstanceProgress(instanceId);
    }

    private void processOne(OfflineDatabaseScanDispatchPayload payload, Map<String, Object> asset) {
        Long assetId = toLong(asset.get("assetId"));
        if (assetId == null) {
            assetId = toLong(asset.get("id"));
        }
        if (assetId == null) {
            return;
        }
        MySQLTableInfo tableInfo = mySQLTableInfoService.getById(assetId);
        if (tableInfo == null) {
            return;
        }
        List<DatabasePolicyTestRulesRequest.TestData> samples = parseSamples(asset.get("columnSamples"));
        if (samples.isEmpty()) {
            samples = extractSamplesFromColumnScanInfo(tableInfo.getColumnScanInfo(), tableInfo);
        }
        AiResult aiResult = evaluateAi(samples, payload.getPolicies());
        tableInfo.setAiSensitivityLevel(String.valueOf(aiResult.maxLevel()));
        tableInfo.setAiSensitivityTags(String.join(",", aiResult.tags()));
        tableInfo.setColumnAiScanInfo(JSON.toJSONString(aiResult.columnInfo()));
        mySQLTableInfoService.updateById(tableInfo);
        publishAiScanSnapshot(payload, tableInfo, aiResult);
        if (aiResult.maxLevel() > 0) {
            incrementRedis(payload.getInstanceId(), "ai_sensitive");
        }
    }

    private void publishAiScanSnapshot(OfflineDatabaseScanDispatchPayload payload, MySQLTableInfo tableInfo, AiResult aiResult) {
        String engine = payload.getEngine();
        if (!StringUtils.hasText(engine)) {
            engine = "MySQL";
        }
        String dataInstance = tableInfo.getInstance();
        String databaseName = tableInfo.getDatabaseName();
        String tableName = tableInfo.getTableName();
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
        offlineScanSnapshotPublisher.publishTableSnapshot(tableMsg);
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
            offlineScanSnapshotPublisher.publishColumnSnapshot(colMsg);
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

    private AiResult evaluateAi(List<DatabasePolicyTestRulesRequest.TestData> samples, List<OfflinePolicySnapshot> policies) {
        int maxLevel = 0;
        Set<String> tags = new LinkedHashSet<>();
        List<AssetScanResult.ColumnScanInfoItem> columnInfo = new ArrayList<>();
        if (samples == null || samples.isEmpty() || policies == null || policies.isEmpty()) {
            return new AiResult(maxLevel, tags, columnInfo);
        }
        for (DatabasePolicyTestRulesRequest.TestData sample : samples) {
            int columnLevel = 0;
            Set<String> columnTags = new LinkedHashSet<>();
            List<String> sensitiveSamples = new ArrayList<>();
            for (OfflinePolicySnapshot p : policies) {
                if (!StringUtils.hasText(p.getAiRule())) {
                    continue;
                }
                AiRuleLlmService.AiRuleResult result = aiRuleLlmService.evaluate("MySQL", p.getAiRule(), List.of(sample));
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
        return new AiResult(maxLevel, tags, columnInfo);
    }

    private List<DatabasePolicyTestRulesRequest.TestData> parseSamples(Object value) {
        if (value == null) {
            return List.of();
        }
        try {
            return JSON.parseObject(JSON.toJSONString(value), new TypeReference<List<DatabasePolicyTestRulesRequest.TestData>>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<DatabasePolicyTestRulesRequest.TestData> extractSamplesFromColumnScanInfo(String columnScanInfo, MySQLTableInfo tableInfo) {
        if (!StringUtils.hasText(columnScanInfo)) {
            return List.of();
        }
        try {
            List<AssetScanResult.ColumnScanInfoItem> items = JSON.parseObject(columnScanInfo, new TypeReference<List<AssetScanResult.ColumnScanInfoItem>>() {
            });
            List<DatabasePolicyTestRulesRequest.TestData> list = new ArrayList<>();
            for (AssetScanResult.ColumnScanInfoItem item : items) {
                DatabasePolicyTestRulesRequest.TestData td = new DatabasePolicyTestRulesRequest.TestData();
                td.setDatabaseName(tableInfo.getDatabaseName());
                td.setTableName(tableInfo.getTableName());
                td.setColumnName(item.columnName());
                td.setColumnValues(item.samples() == null ? List.of() : item.samples());
                list.add(td);
            }
            return list;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private void incrementRedis(Long instanceId, String metric) {
        if (instanceId == null) {
            return;
        }
        stringRedisTemplate.opsForValue().increment("offline-scan:instance:" + instanceId + ":" + metric);
    }

    private long getRedisCount(Long instanceId, String metric) {
        String value = stringRedisTemplate.opsForValue().get("offline-scan:instance:" + instanceId + ":" + metric);
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return 0L;
        }
    }

    private void syncAiInstanceProgress(Long instanceId) {
        if (instanceId == null) {
            return;
        }
        DbAssetMysqlScanOfflineJobInstance inst = jobInstanceService.getById(instanceId);
        if (inst == null) {
            return;
        }
        long success = getRedisCount(instanceId, "ai_success");
        long fail = getRedisCount(instanceId, "ai_fail");
        long sensitive = getRedisCount(instanceId, "ai_sensitive");
        int submitted = inst.getAiSubmittedTotal() == null ? 0 : inst.getAiSubmittedTotal();
        long cappedSuccess = submitted > 0 ? Math.min(success, submitted) : success;
        long cappedFail = submitted > 0 ? Math.min(fail, Math.max(0, submitted - cappedSuccess)) : fail;
        inst.setAiSuccessCount((int) Math.min(Integer.MAX_VALUE, cappedSuccess));
        inst.setAiFailCount((int) Math.min(Integer.MAX_VALUE, cappedFail));
        inst.setAiSensitiveCount((int) Math.min(Integer.MAX_VALUE, sensitive));
        if (inst.getRunStatus() == null || OfflineJobRunStatusEnum.RUNNING.getValue().equals(inst.getRunStatus())) {
            // 主扫描完成且AI也完成时，保持 completed；否则维持当前状态
            int mainSubmitted = inst.getSubmittedTotal() == null ? 0 : inst.getSubmittedTotal();
            int mainDone = (inst.getSuccessCount() == null ? 0 : inst.getSuccessCount()) + (inst.getFailCount() == null ? 0 : inst.getFailCount());
            if (mainSubmitted > 0 && mainDone >= mainSubmitted && submitted > 0 && cappedSuccess + cappedFail >= submitted) {
                inst.setRunStatus(OfflineJobRunStatusEnum.COMPLETED.getValue());
            }
        }
        jobInstanceService.updateById(inst);
    }

    private Long toLong(Object v) {
        if (v == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception ex) {
            return null;
        }
    }

    private record AiResult(int maxLevel, Set<String> tags, List<AssetScanResult.ColumnScanInfoItem> columnInfo) {
    }
}
