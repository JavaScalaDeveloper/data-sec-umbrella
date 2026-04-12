package com.arelore.data.sec.umbrella.server.manager.overview;

import com.alibaba.fastjson2.JSON;
import com.arelore.data.sec.umbrella.server.core.constant.OfflineScanJobDatabaseType;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.ClickhouseTableInfo;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.DataSource;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.DbAssetMysqlScanOfflineJob;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.DbAssetMysqlScanOfflineJobInstance;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.MySQLTableInfo;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.OverviewMetricSnapshot;
import com.arelore.data.sec.umbrella.server.core.enums.MetricPeriodEnum;
import com.arelore.data.sec.umbrella.server.core.enums.ManualReviewLabelEnum;
import com.arelore.data.sec.umbrella.server.core.enums.OverviewMetricCodeEnum;
import com.arelore.data.sec.umbrella.server.core.service.ClickhouseDatabaseInfoService;
import com.arelore.data.sec.umbrella.server.core.service.ClickhouseTableInfoService;
import com.arelore.data.sec.umbrella.server.core.service.DataSourceService;
import com.arelore.data.sec.umbrella.server.core.service.DatabasePolicyService;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetMysqlScanOfflineJobInstanceService;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetMysqlScanOfflineJobService;
import com.arelore.data.sec.umbrella.server.core.service.MySQLDatabaseInfoService;
import com.arelore.data.sec.umbrella.server.core.service.MySQLTableInfoService;
import com.arelore.data.sec.umbrella.server.core.service.OverviewMetricSnapshotService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 概览指标聚合器：负责计算并落库日级指标快照。
 *
 * @author 黄佳豪
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OverviewMetricAggregator {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final DatabasePolicyService databasePolicyService;
    private final DataSourceService dataSourceService;
    private final DbAssetMysqlScanOfflineJobService offlineJobService;
    private final DbAssetMysqlScanOfflineJobInstanceService offlineJobInstanceService;
    private final MySQLDatabaseInfoService mySQLDatabaseInfoService;
    private final MySQLTableInfoService mySQLTableInfoService;
    private final ClickhouseDatabaseInfoService clickhouseDatabaseInfoService;
    private final ClickhouseTableInfoService clickhouseTableInfoService;
    private final OverviewMetricSnapshotService metricSnapshotService;

    /**
     * 按给定日期聚合 MySQL 日级概览指标并 upsert。
     *
     * @param targetDate 统计日期（自然日）
     */
    public void aggregateMysqlDaily(LocalDate targetDate) {
        String metricTime = DAY_FMT.format(targetDate);
        String period = MetricPeriodEnum.DAY.name();

        int policyTotal = (int) databasePolicyService.getAll().stream()
                .filter(p -> "MySQL".equalsIgnoreCase(p.getDatabaseType()))
                .count();

        LambdaQueryWrapper<DataSource> dsQw = new LambdaQueryWrapper<>();
        dsQw.eq(DataSource::getDataSourceType, "MySQL");
        List<DataSource> dsRows = dataSourceService.list(dsQw);
        int dataSourceTotal = dsRows.size();
        Set<String> instances = new HashSet<>();
        for (DataSource ds : dsRows) {
            if (StringUtils.hasText(ds.getInstance())) {
                instances.add(ds.getInstance().trim());
            }
        }
        int instanceTotal = instances.size();

        LambdaQueryWrapper<DbAssetMysqlScanOfflineJob> jobQw = new LambdaQueryWrapper<>();
        int taskTotal = (int) offlineJobService.count(jobQw);
        LambdaQueryWrapper<DbAssetMysqlScanOfflineJob> jobEnabledQw = new LambdaQueryWrapper<>();
        jobEnabledQw.eq(DbAssetMysqlScanOfflineJob::getEnabledStatus, 1);
        int taskEnabledTotal = (int) offlineJobService.count(jobEnabledQw);

        ZoneId zone = ZoneId.systemDefault();
        java.util.Date dayStart = java.util.Date.from(targetDate.atStartOfDay(zone).toInstant());
        java.util.Date nextDayStart = java.util.Date.from(targetDate.plusDays(1).atStartOfDay(zone).toInstant());
        LambdaQueryWrapper<DbAssetMysqlScanOfflineJobInstance> instQw = new LambdaQueryWrapper<>();
        instQw.ge(DbAssetMysqlScanOfflineJobInstance::getCreateTime, dayStart)
                .lt(DbAssetMysqlScanOfflineJobInstance::getCreateTime, nextDayStart);
        int batchTaskInstanceTotal = (int) offlineJobInstanceService.count(instQw);

        int databaseTotal = (int) mySQLDatabaseInfoService.count();
        int tableTotal = (int) mySQLTableInfoService.count();
        LambdaQueryWrapper<MySQLTableInfo> sensitiveQw = new LambdaQueryWrapper<>();
        sensitiveQw.isNotNull(MySQLTableInfo::getSensitivityLevel)
                .ne(MySQLTableInfo::getSensitivityLevel, "")
                .ne(MySQLTableInfo::getSensitivityLevel, "0");
        int sensitiveTableTotal = (int) mySQLTableInfoService.count(sensitiveQw);
        String sensitiveRatio = tableTotal <= 0
                ? "0"
                : new BigDecimal(String.valueOf(sensitiveTableTotal))
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(String.valueOf(tableTotal)), 2, RoundingMode.HALF_UP)
                .toPlainString();

        LambdaQueryWrapper<MySQLTableInfo> sensitiveExcludeIgnoreQw = new LambdaQueryWrapper<>();
        sensitiveExcludeIgnoreQw.isNotNull(MySQLTableInfo::getSensitivityLevel)
                .ne(MySQLTableInfo::getSensitivityLevel, "")
                .ne(MySQLTableInfo::getSensitivityLevel, "0")
                .and(w -> w.isNull(MySQLTableInfo::getManualReview)
                        .or()
                        .ne(MySQLTableInfo::getManualReview, ManualReviewLabelEnum.IGNORE.getCode()));
        int sensitiveExcludeIgnoreTotal = (int) mySQLTableInfoService.count(sensitiveExcludeIgnoreQw);
        String sensitiveExcludeIgnoreRatio = tableTotal <= 0
                ? "0"
                : new BigDecimal(String.valueOf(sensitiveExcludeIgnoreTotal))
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(String.valueOf(tableTotal)), 2, RoundingMode.HALF_UP)
                .toPlainString();

        Map<String, Integer> levelDist = buildLevelDistribution();
        Map<String, Integer> tagDist = buildTagDistribution();

        List<OverviewMetricSnapshot> rows = new ArrayList<>();
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.MYSQL_POLICY_TOTAL, String.valueOf(policyTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.MYSQL_TASK_TOTAL, String.valueOf(taskTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.MYSQL_TASK_ENABLED_TOTAL, String.valueOf(taskEnabledTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.MYSQL_DATASOURCE_TOTAL, String.valueOf(dataSourceTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.MYSQL_BATCH_TASK_TOTAL, String.valueOf(taskTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.MYSQL_BATCH_TASK_INSTANCE_TOTAL, String.valueOf(batchTaskInstanceTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.MYSQL_INSTANCE_TOTAL, String.valueOf(instanceTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.MYSQL_DATABASE_TOTAL, String.valueOf(databaseTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.MYSQL_TABLE_TOTAL, String.valueOf(tableTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.MYSQL_SENSITIVE_TABLE_TOTAL, String.valueOf(sensitiveTableTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.MYSQL_SENSITIVE_TABLE_RATIO, sensitiveRatio));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.MYSQL_SENSITIVE_TABLE_EXCLUDE_IGNORE_TOTAL, String.valueOf(sensitiveExcludeIgnoreTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.MYSQL_SENSITIVE_TABLE_EXCLUDE_IGNORE_RATIO, sensitiveExcludeIgnoreRatio));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.MYSQL_SENSITIVITY_LEVEL_DISTRIBUTION, JSON.toJSONString(levelDist)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.MYSQL_SENSITIVITY_TAG_DISTRIBUTION, JSON.toJSONString(tagDist)));

        metricSnapshotService.upsertBatch(rows);
        log.info("aggregate mysql overview metrics success, date={}, size={}", metricTime, rows.size());
    }

    /**
     * 按给定日期聚合 ClickHouse 日级概览指标并 upsert。
     */
    public void aggregateClickhouseDaily(LocalDate targetDate) {
        String metricTime = DAY_FMT.format(targetDate);
        String period = MetricPeriodEnum.DAY.name();

        int policyTotal = (int) databasePolicyService.getAll().stream()
                .filter(p -> OfflineScanJobDatabaseType.CLICKHOUSE.equals(
                        OfflineScanJobDatabaseType.normalizeJob(p.getDatabaseType())))
                .count();

        LambdaQueryWrapper<DataSource> dsCh = new LambdaQueryWrapper<>();
        dsCh.eq(DataSource::getDataSourceType, OfflineScanJobDatabaseType.CLICKHOUSE);
        List<DataSource> dsRows = dataSourceService.list(dsCh);
        int dataSourceTotal = dsRows.size();
        Set<String> instances = new HashSet<>();
        for (DataSource ds : dsRows) {
            if (StringUtils.hasText(ds.getInstance())) {
                instances.add(ds.getInstance().trim());
            }
        }
        int instanceTotal = instances.size();

        LambdaQueryWrapper<DbAssetMysqlScanOfflineJob> jobCh = clickhouseJobTypeWrapper();
        int taskTotal = (int) offlineJobService.count(jobCh);
        LambdaQueryWrapper<DbAssetMysqlScanOfflineJob> jobEnabledCh = clickhouseJobTypeWrapper();
        jobEnabledCh.eq(DbAssetMysqlScanOfflineJob::getEnabledStatus, 1);
        int taskEnabledTotal = (int) offlineJobService.count(jobEnabledCh);

        ZoneId zone = ZoneId.systemDefault();
        java.util.Date dayStart = java.util.Date.from(targetDate.atStartOfDay(zone).toInstant());
        java.util.Date nextDayStart = java.util.Date.from(targetDate.plusDays(1).atStartOfDay(zone).toInstant());
        LambdaQueryWrapper<DbAssetMysqlScanOfflineJobInstance> instCh = clickhouseInstanceTypeWrapper();
        instCh.ge(DbAssetMysqlScanOfflineJobInstance::getCreateTime, dayStart)
                .lt(DbAssetMysqlScanOfflineJobInstance::getCreateTime, nextDayStart);
        int batchTaskInstanceTotal = (int) offlineJobInstanceService.count(instCh);

        int databaseTotal = (int) clickhouseDatabaseInfoService.count();
        int tableTotal = (int) clickhouseTableInfoService.count();
        LambdaQueryWrapper<ClickhouseTableInfo> sensitiveQw = new LambdaQueryWrapper<>();
        sensitiveQw.isNotNull(ClickhouseTableInfo::getSensitivityLevel)
                .ne(ClickhouseTableInfo::getSensitivityLevel, "")
                .ne(ClickhouseTableInfo::getSensitivityLevel, "0");
        int sensitiveTableTotal = (int) clickhouseTableInfoService.count(sensitiveQw);
        String sensitiveRatio = tableTotal <= 0
                ? "0"
                : new BigDecimal(String.valueOf(sensitiveTableTotal))
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(String.valueOf(tableTotal)), 2, RoundingMode.HALF_UP)
                .toPlainString();

        LambdaQueryWrapper<ClickhouseTableInfo> sensitiveExcludeIgnoreQw = new LambdaQueryWrapper<>();
        sensitiveExcludeIgnoreQw.isNotNull(ClickhouseTableInfo::getSensitivityLevel)
                .ne(ClickhouseTableInfo::getSensitivityLevel, "")
                .ne(ClickhouseTableInfo::getSensitivityLevel, "0")
                .and(w -> w.isNull(ClickhouseTableInfo::getManualReview)
                        .or()
                        .ne(ClickhouseTableInfo::getManualReview, ManualReviewLabelEnum.IGNORE.getCode()));
        int sensitiveExcludeIgnoreTotal = (int) clickhouseTableInfoService.count(sensitiveExcludeIgnoreQw);
        String sensitiveExcludeIgnoreRatio = tableTotal <= 0
                ? "0"
                : new BigDecimal(String.valueOf(sensitiveExcludeIgnoreTotal))
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(String.valueOf(tableTotal)), 2, RoundingMode.HALF_UP)
                .toPlainString();

        Map<String, Integer> levelDist = buildClickhouseLevelDistribution();
        Map<String, Integer> tagDist = buildClickhouseTagDistribution();

        List<OverviewMetricSnapshot> rows = new ArrayList<>();
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.CLICKHOUSE_POLICY_TOTAL, String.valueOf(policyTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.CLICKHOUSE_TASK_TOTAL, String.valueOf(taskTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.CLICKHOUSE_TASK_ENABLED_TOTAL, String.valueOf(taskEnabledTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.CLICKHOUSE_DATASOURCE_TOTAL, String.valueOf(dataSourceTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.CLICKHOUSE_BATCH_TASK_TOTAL, String.valueOf(taskTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.CLICKHOUSE_BATCH_TASK_INSTANCE_TOTAL, String.valueOf(batchTaskInstanceTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.CLICKHOUSE_INSTANCE_TOTAL, String.valueOf(instanceTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.CLICKHOUSE_DATABASE_TOTAL, String.valueOf(databaseTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.CLICKHOUSE_TABLE_TOTAL, String.valueOf(tableTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.CLICKHOUSE_SENSITIVE_TABLE_TOTAL, String.valueOf(sensitiveTableTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.CLICKHOUSE_SENSITIVE_TABLE_RATIO, sensitiveRatio));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.CLICKHOUSE_SENSITIVE_TABLE_EXCLUDE_IGNORE_TOTAL, String.valueOf(sensitiveExcludeIgnoreTotal)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.CLICKHOUSE_SENSITIVE_TABLE_EXCLUDE_IGNORE_RATIO, sensitiveExcludeIgnoreRatio));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.CLICKHOUSE_SENSITIVITY_LEVEL_DISTRIBUTION, JSON.toJSONString(levelDist)));
        rows.add(metric(period, metricTime, OverviewMetricCodeEnum.CLICKHOUSE_SENSITIVITY_TAG_DISTRIBUTION, JSON.toJSONString(tagDist)));

        metricSnapshotService.upsertBatch(rows);
        log.info("aggregate clickhouse overview metrics success, date={}, size={}", metricTime, rows.size());
    }

    private static LambdaQueryWrapper<DbAssetMysqlScanOfflineJob> clickhouseJobTypeWrapper() {
        LambdaQueryWrapper<DbAssetMysqlScanOfflineJob> w = new LambdaQueryWrapper<>();
        w.and(q -> q.eq(DbAssetMysqlScanOfflineJob::getDatabaseType, OfflineScanJobDatabaseType.CLICKHOUSE)
                .or().eq(DbAssetMysqlScanOfflineJob::getDatabaseType, "ClickHouse"));
        return w;
    }

    private static LambdaQueryWrapper<DbAssetMysqlScanOfflineJobInstance> clickhouseInstanceTypeWrapper() {
        LambdaQueryWrapper<DbAssetMysqlScanOfflineJobInstance> w = new LambdaQueryWrapper<>();
        w.and(q -> q.eq(DbAssetMysqlScanOfflineJobInstance::getDatabaseType, OfflineScanJobDatabaseType.CLICKHOUSE)
                .or().eq(DbAssetMysqlScanOfflineJobInstance::getDatabaseType, "ClickHouse"));
        return w;
    }

    private OverviewMetricSnapshot metric(String period, String metricTime, OverviewMetricCodeEnum code, String value) {
        OverviewMetricSnapshot row = new OverviewMetricSnapshot();
        row.setMetricCode(code.getCode());
        row.setMetricPeriod(period);
        row.setMetricTime(metricTime);
        row.setMetricValue(value);
        row.setDescription(code.getDescription());
        return row;
    }

    private Map<String, Integer> buildLevelDistribution() {
        Map<String, Integer> levelDist = new HashMap<>();
        levelDist.put("1", 0);
        levelDist.put("2", 0);
        levelDist.put("3", 0);
        levelDist.put("4", 0);
        levelDist.put("5", 0);
        List<MySQLTableInfo> rows = mySQLTableInfoService.list();
        for (MySQLTableInfo row : rows) {
            String level = row.getSensitivityLevel();
            if (!StringUtils.hasText(level) || "0".equals(level)) {
                continue;
            }
            levelDist.put(level, levelDist.getOrDefault(level, 0) + 1);
        }
        return levelDist;
    }

    private Map<String, Integer> buildTagDistribution() {
        Map<String, Integer> tagDist = new HashMap<>();
        List<MySQLTableInfo> rows = mySQLTableInfoService.list();
        for (MySQLTableInfo row : rows) {
            if (!StringUtils.hasText(row.getSensitivityTags())) {
                continue;
            }
            String[] tags = row.getSensitivityTags().split(",");
            for (String raw : tags) {
                String tag = raw == null ? "" : raw.trim();
                if (!StringUtils.hasText(tag)) {
                    continue;
                }
                tagDist.put(tag, tagDist.getOrDefault(tag, 0) + 1);
            }
        }
        // top 50 限制，避免单条指标值过长
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(tagDist.entrySet());
        sorted.sort(Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed());
        Map<String, Integer> top = new HashMap<>();
        int i = 0;
        for (Map.Entry<String, Integer> it : sorted) {
            top.put(it.getKey(), it.getValue());
            i++;
            if (i >= 50) {
                break;
            }
        }
        return top;
    }

    private Map<String, Integer> buildClickhouseLevelDistribution() {
        Map<String, Integer> levelDist = new HashMap<>();
        levelDist.put("1", 0);
        levelDist.put("2", 0);
        levelDist.put("3", 0);
        levelDist.put("4", 0);
        levelDist.put("5", 0);
        List<ClickhouseTableInfo> rows = clickhouseTableInfoService.list();
        for (ClickhouseTableInfo row : rows) {
            String level = row.getSensitivityLevel();
            if (!StringUtils.hasText(level) || "0".equals(level)) {
                continue;
            }
            levelDist.put(level, levelDist.getOrDefault(level, 0) + 1);
        }
        return levelDist;
    }

    private Map<String, Integer> buildClickhouseTagDistribution() {
        Map<String, Integer> tagDist = new HashMap<>();
        List<ClickhouseTableInfo> rows = clickhouseTableInfoService.list();
        for (ClickhouseTableInfo row : rows) {
            if (!StringUtils.hasText(row.getSensitivityTags())) {
                continue;
            }
            String[] tags = row.getSensitivityTags().split(",");
            for (String raw : tags) {
                String tag = raw == null ? "" : raw.trim();
                if (!StringUtils.hasText(tag)) {
                    continue;
                }
                tagDist.put(tag, tagDist.getOrDefault(tag, 0) + 1);
            }
        }
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(tagDist.entrySet());
        sorted.sort(Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed());
        Map<String, Integer> top = new HashMap<>();
        int i = 0;
        for (Map.Entry<String, Integer> it : sorted) {
            top.put(it.getKey(), it.getValue());
            i++;
            if (i >= 50) {
                break;
            }
        }
        return top;
    }
}

