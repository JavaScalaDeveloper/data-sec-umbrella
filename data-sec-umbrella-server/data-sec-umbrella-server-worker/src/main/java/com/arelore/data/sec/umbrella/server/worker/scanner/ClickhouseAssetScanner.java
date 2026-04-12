package com.arelore.data.sec.umbrella.server.worker.scanner;

import com.alibaba.fastjson2.JSON;
import com.arelore.data.sec.umbrella.server.core.constant.RSAKeyConstants;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineDatabaseScanDispatchPayload;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineJobConfigSnapshot;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflinePolicySnapshot;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyAssetSample;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyClassificationRule;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyRuleDetectionRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.DatabasePolicyRuleDetectionResponse;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.ClickhouseTableInfo;
import com.arelore.data.sec.umbrella.server.core.service.ClickhouseTableInfoService;
import com.arelore.data.sec.umbrella.server.core.service.checker.RulesChecker;
import com.arelore.data.sec.umbrella.server.core.service.factory.RulesCheckerFactory;
import com.arelore.data.sec.umbrella.server.core.util.ClickHouseJdbcUrl;
import com.arelore.data.sec.umbrella.server.core.util.JdbcSampleCellFormatter;
import com.arelore.data.sec.umbrella.server.core.util.RSACryptoUtil;
import com.arelore.data.sec.umbrella.server.worker.task.OfflineAiScanDispatchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * ClickHouse 资产扫描器：取样、规则判断、回写 db_asset_clickhouse_table_info。
 */
public class ClickhouseAssetScanner implements AssetScanner {

    private static final int SAMPLE_LIMIT_DEFAULT = 10;
    private static final int SAMPLE_LIMIT_MAX = 5000;
    private static final String CHECKER_KEY = "Clickhouse";

    private final ClickhouseTableInfoService clickhouseTableInfoService;
    private final OfflineAiScanDispatchService offlineAiScanDispatchService;

    @Override
    public String databaseType() {
        return "Clickhouse";
    }

    @Override
    public AssetScanResult scan(OfflineDatabaseScanDispatchPayload payload, Map<String, Object> asset) {
        Long assetId = toLong(asset.get("id"));
        String instance = str(asset.get("instance"));
        String databaseName = str(asset.get("databaseName"));
        String tableName = str(asset.get("tableName"));

        boolean needFetch = payload.getJobConfig() != null && Integer.valueOf(1).equals(payload.getJobConfig().getEnableSampling());
        List<DatabasePolicyAssetSample> samples = needFetch
                ? fetchSamples(payload, instance, databaseName, tableName)
                : buildEmptySample(databaseName, tableName);

        List<OfflinePolicySnapshot> policies = filterPoliciesForCurrentDatabaseType(payload.getPolicies());
        RuleResult result = evaluatePolicies(samples, policies);
        List<AssetScanResult.ColumnScanInfoItem> columnScanInfo = buildColumnScanInfo(samples, policies);
        updateClickhouseTableAsset(instance, databaseName, tableName, result, columnScanInfo);
        AssetScanResult scanResult = new AssetScanResult(
                result.maxLevel() > 0,
                assetId,
                "Clickhouse",
                result.maxLevel(),
                new ArrayList<>(result.tags()),
                columnScanInfo,
                samples
        );
        offlineAiScanDispatchService.dispatchIfNeeded(payload, asset, scanResult);
        return scanResult;
    }

    private List<DatabasePolicyAssetSample> fetchSamples(
            OfflineDatabaseScanDispatchPayload payload,
            String instance,
            String db,
            String table
    ) {
        try (Connection conn = openClickhouseConnection(payload, instance, db)) {
            if (conn == null) {
                return buildEmptySample(db, table);
            }
            int limit = resolveSampleLimit(payload.getJobConfig());
            String mode = normalizeSampleMode(payload.getJobConfig());
            // ClickHouse 表多数无稳定 id；正序/倒序均不排序，仅 LIMIT；随机模式用 rand()
            String sql = "random".equals(mode)
                    ? "SELECT * FROM " + qualifyTable(db, table) + " ORDER BY rand() LIMIT ?"
                    : "SELECT * FROM " + qualifyTable(db, table) + " LIMIT ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    return mergeResultSetRowsIntoSamples(rs, db, table);
                }
            }
        } catch (Exception ex) {
            log.warn("ClickHouse fetch sample failed, instance={}, db={}, table={}, err={}", instance, db, table, ex.getMessage());
            return buildEmptySample(db, table);
        }
    }

    private static int resolveSampleLimit(OfflineJobConfigSnapshot cfg) {
        int n = SAMPLE_LIMIT_DEFAULT;
        if (cfg != null && cfg.getSampleCount() != null && cfg.getSampleCount() > 0) {
            n = cfg.getSampleCount();
        }
        return Math.min(SAMPLE_LIMIT_MAX, n);
    }

    private static String normalizeSampleMode(OfflineJobConfigSnapshot cfg) {
        if (cfg == null || !StringUtils.hasText(cfg.getSampleMode())) {
            return "sequence";
        }
        String raw = cfg.getSampleMode().trim();
        if ("倒序".equals(raw) || "reverse".equalsIgnoreCase(raw)) {
            return "reverse";
        }
        if ("随机".equals(raw) || "random".equalsIgnoreCase(raw)) {
            return "random";
        }
        return "sequence";
    }

    /**
     * 仅使用 MQ 载荷中的 JDBC 凭据（管理端 {@code attachJdbcCredentials} 已写入），Worker 不再查数据源表。
     */
    private Connection openClickhouseConnection(OfflineDatabaseScanDispatchPayload payload, String instance, String db)
            throws Exception {
        String user = payload.getMysqlJdbcUsername();
        String encPwd = payload.getMysqlJdbcPasswordEncrypted();
        if (!StringUtils.hasText(user) || !StringUtils.hasText(encPwd)) {
            log.warn("ClickHouse 离线扫描缺少 payload 中的 JDBC 凭据（mysqlJdbcUsername/mysqlJdbcPasswordEncrypted），instance={}", instance);
            return null;
        }
        String rawPwd = RSACryptoUtil.decrypt(encPwd, RSAKeyConstants.PRIVATE_KEY);
        String database = StringUtils.hasText(db) ? db.trim() : "default";
        return DriverManager.getConnection(ClickHouseJdbcUrl.build(instance, database), user, rawPwd);
    }

    private static List<Map<String, String>> readAllRowMaps(ResultSet rs) throws Exception {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        List<String> labels = new ArrayList<>(colCount);
        for (int i = 1; i <= colCount; i++) {
            labels.add(meta.getColumnLabel(i));
        }
        List<Map<String, String>> rows = new ArrayList<>();
        while (rs.next()) {
            Map<String, String> row = new LinkedHashMap<>();
            for (int i = 1; i <= colCount; i++) {
                Object o = rs.getObject(i);
                row.put(labels.get(i - 1), JdbcSampleCellFormatter.toSampleString(o));
            }
            rows.add(row);
        }
        return rows;
    }

    private static List<DatabasePolicyAssetSample> mergeResultSetRowsIntoSamples(
            ResultSet rs,
            String db,
            String table
    ) throws Exception {
        List<Map<String, String>> rows = readAllRowMaps(rs);
        return mergeRowMapsToSamples(rows, db, table);
    }

    private static List<DatabasePolicyAssetSample> mergeRowMapsToSamples(
            List<Map<String, String>> rows,
            String db,
            String table
    ) {
        if (rows == null || rows.isEmpty()) {
            return buildEmptySample(db, table);
        }
        List<String> colOrder = new ArrayList<>(rows.get(0).keySet());
        Map<String, List<String>> acc = new LinkedHashMap<>();
        for (String col : colOrder) {
            acc.put(col, new ArrayList<>());
        }
        for (Map<String, String> row : rows) {
            for (String col : colOrder) {
                acc.get(col).add(row.getOrDefault(col, ""));
            }
        }
        List<DatabasePolicyAssetSample> list = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : acc.entrySet()) {
            DatabasePolicyAssetSample td = new DatabasePolicyAssetSample();
            td.setDatabaseName(db);
            td.setTableName(table);
            td.setColumnName(e.getKey());
            td.setColumnValues(e.getValue());
            list.add(td);
        }
        return list;
    }

    private static String qualifyTable(String db, String table) {
        return "`" + escapeSqlIdentifier(db) + "`.`" + escapeSqlIdentifier(table) + "`";
    }

    private static String escapeSqlIdentifier(String name) {
        return name == null ? "" : name.replace("`", "``");
    }

    private List<OfflinePolicySnapshot> filterPoliciesForCurrentDatabaseType(List<OfflinePolicySnapshot> policies) {
        if (policies == null || policies.isEmpty()) {
            return policies == null ? List.of() : policies;
        }
        String expected = databaseType();
        return policies.stream()
                .filter(p -> p != null
                        && StringUtils.hasText(p.getDatabaseType())
                        && expected.equalsIgnoreCase(p.getDatabaseType().trim()))
                .collect(Collectors.toList());
    }

    private RuleResult evaluatePolicies(List<DatabasePolicyAssetSample> samples, List<OfflinePolicySnapshot> policies) {
        int maxLevel = 0;
        Set<String> tags = new LinkedHashSet<>();
        RulesChecker checker = RulesCheckerFactory.getRulesChecker(CHECKER_KEY);
        if (checker == null || policies == null) {
            return new RuleResult(maxLevel, tags);
        }
        for (OfflinePolicySnapshot p : policies) {
            DatabasePolicyRuleDetectionRequest req = new DatabasePolicyRuleDetectionRequest();
            req.setDatabaseType(CHECKER_KEY);
            req.setRuleExpression(p.getRuleExpression());
            req.setAiRule(p.getAiRule());
            req.setClassificationRules(parseClassificationRules(p.getClassificationRules()));
            req.setSamples(samples);
            DatabasePolicyRuleDetectionResponse resp = checker.checkRules(req);
            if (resp != null && resp.isRulePassed()) {
                if (p.getSensitivityLevel() != null) {
                    maxLevel = Math.max(maxLevel, p.getSensitivityLevel());
                }
                if (StringUtils.hasText(p.getPolicyCode())) {
                    tags.add(p.getPolicyCode());
                }
            }
        }
        return new RuleResult(maxLevel, tags);
    }

    private List<DatabasePolicyClassificationRule> parseClassificationRules(String rulesJson) {
        if (!StringUtils.hasText(rulesJson)) {
            return List.of();
        }
        try {
            return JSON.parseArray(rulesJson, DatabasePolicyClassificationRule.class);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<AssetScanResult.ColumnScanInfoItem> buildColumnScanInfo(
            List<DatabasePolicyAssetSample> samples,
            List<OfflinePolicySnapshot> policies
    ) {
        if (samples == null || samples.isEmpty()) {
            return List.of();
        }
        RulesChecker checker = RulesCheckerFactory.getRulesChecker(CHECKER_KEY);
        if (checker == null || policies == null || policies.isEmpty()) {
            return samples.stream().map(s -> new AssetScanResult.ColumnScanInfoItem(
                    s.getColumnName(),
                    "0",
                    List.of(),
                    List.of(),
                    s.getColumnValues() == null ? List.of() : s.getColumnValues()
            )).collect(Collectors.toList());
        }
        List<AssetScanResult.ColumnScanInfoItem> info = new ArrayList<>();
        for (DatabasePolicyAssetSample sample : samples) {
            int level = 0;
            Set<String> tags = new LinkedHashSet<>();
            List<String> sensitiveSamples = new ArrayList<>();
            for (OfflinePolicySnapshot p : policies) {
                DatabasePolicyRuleDetectionRequest req = new DatabasePolicyRuleDetectionRequest();
                req.setDatabaseType(CHECKER_KEY);
                req.setRuleExpression(p.getRuleExpression());
                req.setClassificationRules(parseClassificationRules(p.getClassificationRules()));
                req.setSamples(List.of(sample));
                DatabasePolicyRuleDetectionResponse resp = checker.checkRules(req);
                if (resp != null && resp.isRulePassed()) {
                    if (p.getSensitivityLevel() != null) {
                        level = Math.max(level, p.getSensitivityLevel());
                    }
                    if (StringUtils.hasText(p.getPolicyCode())) {
                        tags.add(p.getPolicyCode());
                    }
                    if (sample.getColumnValues() != null) {
                        sensitiveSamples.addAll(sample.getColumnValues());
                    }
                }
            }
            info.add(new AssetScanResult.ColumnScanInfoItem(
                    sample.getColumnName(),
                    String.valueOf(level),
                    new ArrayList<>(tags),
                    sensitiveSamples.stream().distinct().collect(Collectors.toList()),
                    sample.getColumnValues() == null ? List.of() : sample.getColumnValues()
            ));
        }
        return info;
    }

    private void updateClickhouseTableAsset(
            String instance,
            String db,
            String table,
            RuleResult result,
            List<AssetScanResult.ColumnScanInfoItem> columnScanInfo
    ) {
        LambdaQueryWrapper<ClickhouseTableInfo> qw = new LambdaQueryWrapper<>();
        qw.eq(ClickhouseTableInfo::getInstance, instance);
        qw.eq(ClickhouseTableInfo::getDatabaseName, db);
        qw.eq(ClickhouseTableInfo::getTableName, table);
        qw.last("limit 1");
        ClickhouseTableInfo row = clickhouseTableInfoService.getOne(qw);
        if (row == null) {
            return;
        }
        row.setSensitivityLevel(String.valueOf(result.maxLevel()));
        row.setSensitivityTags(String.join(",", result.tags()));
        row.setColumnScanInfo(JSON.toJSONString(columnScanInfo == null ? List.of() : columnScanInfo));
        clickhouseTableInfoService.updateById(row);
    }

    private static List<DatabasePolicyAssetSample> buildEmptySample(String db, String table) {
        DatabasePolicyAssetSample td = new DatabasePolicyAssetSample();
        td.setDatabaseName(db);
        td.setTableName(table);
        td.setColumnValues(List.of());
        return List.of(td);
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value);
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

    private record RuleResult(int maxLevel, Set<String> tags) {
    }
}
