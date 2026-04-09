package com.arelore.data.sec.umbrella.server.worker.scanner;

import com.alibaba.fastjson2.JSON;
import com.arelore.data.sec.umbrella.server.core.constant.RSAKeyConstants;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineMysqlScanDispatchPayload;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflinePolicySnapshot;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyTestRulesRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.DatabasePolicyTestRulesResponse;
import com.arelore.data.sec.umbrella.server.core.entity.DataSource;
import com.arelore.data.sec.umbrella.server.core.entity.MySQLTableInfo;
import com.arelore.data.sec.umbrella.server.core.service.DataSourceService;
import com.arelore.data.sec.umbrella.server.core.service.MySQLTableInfoService;
import com.arelore.data.sec.umbrella.server.core.service.checker.RulesChecker;
import com.arelore.data.sec.umbrella.server.core.service.factory.RulesCheckerFactory;
import com.arelore.data.sec.umbrella.server.core.util.RSACryptoUtil;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * MySQL 资产扫描器，负责取样、规则判断与资产敏感信息回写。
 *
 * @author 黄佳豪
 */
public class MySQLAssetScanner implements AssetScanner {

    private final DataSourceService dataSourceService;
    private final MySQLTableInfoService mySQLTableInfoService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String databaseType() {
        return "MySQL";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AssetScanResult scan(OfflineMysqlScanDispatchPayload payload, Map<String, Object> asset) {
        Long assetId = toLong(asset.get("id"));
        String instance = str(asset.get("instance"));
        String databaseName = str(asset.get("databaseName"));
        String tableName = str(asset.get("tableName"));

        boolean needFetch = payload.getJobConfig() != null && Integer.valueOf(1).equals(payload.getJobConfig().getEnableSampling());
        List<DatabasePolicyTestRulesRequest.TestData> samples = needFetch
                ? fetchSamples(instance, databaseName, tableName)
                : buildEmptySample(databaseName, tableName);

        RuleResult result = evaluatePolicies(samples, payload.getPolicies());
        List<AssetScanResult.ColumnScanInfoItem> columnScanInfo = buildColumnScanInfo(samples, payload.getPolicies());
        updateMysqlTableAsset(instance, databaseName, tableName, result, columnScanInfo);
        return new AssetScanResult(
                result.maxLevel() > 0,
                assetId,
                "MySQL",
                result.maxLevel(),
                new ArrayList<>(result.tags()),
                columnScanInfo,
                samples
        );
    }

    private List<DatabasePolicyTestRulesRequest.TestData> fetchSamples(String instance, String db, String table) {
        try {
            LambdaQueryWrapper<DataSource> w = new LambdaQueryWrapper<>();
            w.eq(DataSource::getInstance, instance);
            w.eq(DataSource::getDataSourceType, "MySQL");
            w.orderByDesc(DataSource::getId);
            w.last("limit 1");
            DataSource ds = dataSourceService.getOne(w);
            if (ds == null || !StringUtils.hasText(ds.getUsername()) || !StringUtils.hasText(ds.getPassword())) {
                return buildEmptySample(db, table);
            }
            String rawPwd = RSACryptoUtil.decrypt(ds.getPassword(), RSAKeyConstants.PRIVATE_KEY);
            String url = "jdbc:mysql://" + instance + "/" + db + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
            String sql = "SELECT * FROM `" + db + "`.`" + table + "` LIMIT 1";
            try (Connection conn = DriverManager.getConnection(url, ds.getUsername(), rawPwd);
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return buildEmptySample(db, table);
                }
                ResultSetMetaData metaData = rs.getMetaData();
                int count = metaData.getColumnCount();
                List<DatabasePolicyTestRulesRequest.TestData> list = new ArrayList<>();
                for (int i = 1; i <= count; i++) {
                    DatabasePolicyTestRulesRequest.TestData td = new DatabasePolicyTestRulesRequest.TestData();
                    td.setDatabaseName(db);
                    td.setTableName(table);
                    td.setColumnName(metaData.getColumnLabel(i));
                    Object value = rs.getObject(i);
                    td.setColumnValues(value == null ? List.of() : List.of(String.valueOf(value)));
                    list.add(td);
                }
                return list;
            }
        } catch (Exception ex) {
            log.warn("fetch sample failed, instance={}, db={}, table={}, err={}", instance, db, table, ex.getMessage());
            return buildEmptySample(db, table);
        }
    }

    private RuleResult evaluatePolicies(List<DatabasePolicyTestRulesRequest.TestData> samples, List<OfflinePolicySnapshot> policies) {
        int maxLevel = 0;
        Set<String> tags = new LinkedHashSet<>();
        RulesChecker checker = RulesCheckerFactory.getRulesChecker("MySQL");
        if (checker == null || policies == null) {
            return new RuleResult(maxLevel, tags);
        }
        for (OfflinePolicySnapshot p : policies) {
            DatabasePolicyTestRulesRequest req = new DatabasePolicyTestRulesRequest();
            req.setDatabaseType("MySQL");
            req.setRuleExpression(p.getRuleExpression());
            req.setAiRule(p.getAiRule());
            req.setClassificationRules(parseClassificationRules(p.getClassificationRules()));
            req.setTestData(samples);
            DatabasePolicyTestRulesResponse resp = checker.checkRules(req);
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

    private List<DatabasePolicyTestRulesRequest.ClassificationRule> parseClassificationRules(String rulesJson) {
        if (!StringUtils.hasText(rulesJson)) {
            return List.of();
        }
        try {
            return JSON.parseArray(rulesJson, DatabasePolicyTestRulesRequest.ClassificationRule.class);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<AssetScanResult.ColumnScanInfoItem> buildColumnScanInfo(
            List<DatabasePolicyTestRulesRequest.TestData> samples,
            List<OfflinePolicySnapshot> policies
    ) {
        if (samples == null || samples.isEmpty()) {
            return List.of();
        }
        RulesChecker checker = RulesCheckerFactory.getRulesChecker("MySQL");
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
        for (DatabasePolicyTestRulesRequest.TestData sample : samples) {
            int level = 0;
            Set<String> tags = new LinkedHashSet<>();
            List<String> sensitiveSamples = new ArrayList<>();
            for (OfflinePolicySnapshot p : policies) {
                DatabasePolicyTestRulesRequest req = new DatabasePolicyTestRulesRequest();
                req.setDatabaseType("MySQL");
                req.setRuleExpression(p.getRuleExpression());
                req.setClassificationRules(parseClassificationRules(p.getClassificationRules()));
                req.setTestData(List.of(sample));
                DatabasePolicyTestRulesResponse resp = checker.checkRules(req);
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

    private void updateMysqlTableAsset(String instance, String db, String table, RuleResult result,
                                       List<AssetScanResult.ColumnScanInfoItem> columnScanInfo) {
        LambdaQueryWrapper<MySQLTableInfo> qw = new LambdaQueryWrapper<>();
        qw.eq(MySQLTableInfo::getInstance, instance);
        qw.eq(MySQLTableInfo::getDatabaseName, db);
        qw.eq(MySQLTableInfo::getTableName, table);
        qw.last("limit 1");
        MySQLTableInfo row = mySQLTableInfoService.getOne(qw);
        if (row == null) {
            return;
        }
        row.setSensitivityLevel(String.valueOf(result.maxLevel()));
        row.setSensitivityTags(String.join(",", result.tags()));
        row.setColumnScanInfo(JSON.toJSONString(columnScanInfo == null ? List.of() : columnScanInfo));
        mySQLTableInfoService.updateById(row);
    }

    private List<DatabasePolicyTestRulesRequest.TestData> buildEmptySample(String db, String table) {
        DatabasePolicyTestRulesRequest.TestData td = new DatabasePolicyTestRulesRequest.TestData();
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

