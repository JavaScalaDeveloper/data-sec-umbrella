package com.arelore.data.sec.umbrella.server.manager.task;

import com.alibaba.fastjson2.JSON;
import com.arelore.data.sec.umbrella.server.core.constant.OfflineScanConstants;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineJobConfigSnapshot;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineDatabaseScanDispatchPayload;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflinePolicySnapshot;
import com.arelore.data.sec.umbrella.server.core.dto.response.DatabasePolicyResponse;
import com.arelore.data.sec.umbrella.server.core.entity.DbAssetMysqlScanOfflineJob;
import com.arelore.data.sec.umbrella.server.core.entity.DbAssetMysqlScanOfflineJobInstance;
import com.arelore.data.sec.umbrella.server.core.enums.OfflineJobRunStatusEnum;
import com.arelore.data.sec.umbrella.server.core.service.DatabasePolicyService;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetMysqlScanOfflineJobService;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetMysqlScanOfflineJobInstanceService;
import com.arelore.data.sec.umbrella.server.core.manager.task.TaskManager;
import com.arelore.data.sec.umbrella.server.manager.task.asset.AssetPage;
import com.arelore.data.sec.umbrella.server.manager.task.asset.AssetQueryStrategy;
import com.arelore.data.sec.umbrella.server.manager.task.asset.MySQLTableAssetQueryStrategy;
import com.arelore.data.sec.umbrella.server.manager.task.infra.RabbitDispatchUtil;
import com.arelore.data.sec.umbrella.server.manager.task.infra.RedisTaskCacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 离线扫描任务分发（管理端进程内执行：查库 → MQ → Redis）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskManagerImpl implements TaskManager {

    private final DbAssetMysqlScanOfflineJobInstanceService jobInstanceService;
    private final DbAssetMysqlScanOfflineJobService offlineJobService;
    private final DatabasePolicyService databasePolicyService;
    private final MySQLTableAssetQueryStrategy mySQLAssetQueryStrategy;
    private final RabbitDispatchUtil rabbitDispatchUtil;
    private final RedisTaskCacheUtil redisTaskCacheUtil;

    @Override
    public void dispatchOfflineMysqlScan() {
        List<DbAssetMysqlScanOfflineJobInstance> waiting = jobInstanceService.listWaitingInstances();
        if (waiting.isEmpty()) {
            return;
        }

        List<DatabasePolicyResponse> allPolicies = databasePolicyService.getAll();
        if (allPolicies == null || allPolicies.isEmpty()) {
            failAll(waiting, "database_policy 中无任何规则");
            return;
        }

        for (DbAssetMysqlScanOfflineJobInstance inst : waiting) {
            try {
                dispatchOne(inst, allPolicies);
            } catch (Exception e) {
                log.error("dispatch instance {} failed", inst.getId(), e);
                markFailed(inst, e.getMessage());
            }
        }
    }

    private void failAll(List<DbAssetMysqlScanOfflineJobInstance> list, String reason) {
        for (DbAssetMysqlScanOfflineJobInstance inst : list) {
            markFailed(inst, reason);
        }
    }

    private void markFailed(DbAssetMysqlScanOfflineJobInstance inst, String reason) {
        inst.setRunStatus(OfflineJobRunStatusEnum.FAILED.getValue());
        inst.setExtendInfo(JSON.toJSONString(Map.of("reason", reason != null ? reason : "unknown")));
        jobInstanceService.updateById(inst);
    }

    private void dispatchOne(DbAssetMysqlScanOfflineJobInstance inst, List<DatabasePolicyResponse> allPolicies)
            throws Exception {
        DbAssetMysqlScanOfflineJob job = offlineJobService.findLatestByTaskName(inst.getTaskName());
        if (job == null) {
            markFailed(inst, "关联的离线任务不存在");
            return;
        }

        Set<String> tagFilter = parseStringSet(job.getSupportedTags());
        List<DatabasePolicyResponse> policies = allPolicies;
        if (!tagFilter.isEmpty()) {
            policies = allPolicies.stream()
                    .filter(p -> p.getPolicyCode() != null && tagFilter.contains(p.getPolicyCode()))
                    .collect(Collectors.toList());
        }
        if (policies.isEmpty()) {
            markFailed(inst, "任务配置的标签未匹配到任何 database_policy 规则");
            return;
        }

        // 策略模式：按数据源类型选择资产查询策略（当前为 MySQL，Clickhouse 后续接入对应策略）
        AssetQueryStrategy assetQueryStrategy = mySQLAssetQueryStrategy;

        long total = assetQueryStrategy.total(job);
        if (total <= 0) {
            markFailed(inst, "未查询到待扫描的表资产（db_asset_mysql_table_info）");
            return;
        }

        int expectedTotal = (int) Math.min(Integer.MAX_VALUE, total);
        inst.setExpectedTotal(expectedTotal);
        inst.setSubmittedTotal(0);
        if (job.getEnableAiScan() != null && job.getEnableAiScan() == 1) {
            inst.setAiExpectedTotal(expectedTotal);
            inst.setAiSubmittedTotal(0);
        } else {
            inst.setAiExpectedTotal(0);
            inst.setAiSubmittedTotal(0);
        }
        jobInstanceService.updateById(inst);

        int submitted = 0;
        long pageSize = 200;
        long current = 1;
        long dispatchVersion = System.currentTimeMillis();
        List<OfflinePolicySnapshot> policySnapshots = policies.stream().map(this::policyToSnapshot).collect(Collectors.toList());
        redisTaskCacheUtil.cacheInstanceToJob(inst.getId(), job.getId(), dispatchVersion);

        while (submitted < expectedTotal) {
            AssetPage page = assetQueryStrategy.page(job, current, pageSize);
            if (page.getRecords() == null || page.getRecords().isEmpty()) {
                break;
            }
            for (Map<String, Object> asset : page.getRecords()) {
                OfflineDatabaseScanDispatchPayload payload = new OfflineDatabaseScanDispatchPayload();
                payload.setEngine("MySQL");
                payload.setInstanceId(inst.getId());
                payload.setJobId(job.getId());
                payload.setTaskName(job.getTaskName());
                payload.setDispatchVersion(dispatchVersion);
                payload.setJobConfig(buildJobConfig(job));
                // 每个表资产一个 msg
                payload.setAssets(List.of(asset));
                payload.setPolicies(policySnapshots);

                String body = JSON.toJSONString(payload);
                rabbitDispatchUtil.sendOfflineScan(body);
                submitted++;
            }
            current++;
        }

        inst.setRunStatus(OfflineJobRunStatusEnum.RUNNING.getValue());
        inst.setSubmittedTotal(submitted);
        if (job.getEnableAiScan() != null && job.getEnableAiScan() == 1) {
            inst.setAiSubmittedTotal(submitted);
        } else {
            inst.setAiSubmittedTotal(0);
        }
        inst.setExtendInfo(JSON.toJSONString(Map.of(
                "exchange", OfflineScanConstants.RABBIT_EXCHANGE,
                "routingKey", OfflineScanConstants.RABBIT_ROUTING_KEY,
                "assetEngine", "MySQL")));
        jobInstanceService.updateById(inst);
        log.info("Dispatched offline scan instance {} job {}", inst.getId(), job.getId());
    }

    private OfflineJobConfigSnapshot buildJobConfig(DbAssetMysqlScanOfflineJob job) {
        OfflineJobConfigSnapshot snapshot = new OfflineJobConfigSnapshot();
        snapshot.setSampleCount(job.getSampleCount());
        snapshot.setSampleMode(job.getSampleMode());
        snapshot.setEnableSampling(job.getEnableSampling());
        snapshot.setEnableAiScan(job.getEnableAiScan());
        snapshot.setScanPeriod(job.getScanPeriod());
        snapshot.setSupportedTags(job.getSupportedTags());
        snapshot.setScanScope(job.getScanScope());
        snapshot.setScanInstanceIds(job.getScanInstanceIds());
        snapshot.setTimeRangeType(job.getTimeRangeType());
        return snapshot;
    }

    private OfflinePolicySnapshot policyToSnapshot(DatabasePolicyResponse p) {
        OfflinePolicySnapshot snapshot = new OfflinePolicySnapshot();
        snapshot.setId(p.getId());
        snapshot.setPolicyCode(p.getPolicyCode());
        snapshot.setPolicyName(p.getPolicyName());
        snapshot.setClassificationRules(p.getClassificationRules());
        snapshot.setRuleExpression(p.getRuleExpression());
        snapshot.setAiRule(p.getAiRule());
        snapshot.setDatabaseType(p.getDatabaseType());
        snapshot.setSensitivityLevel(p.getSensitivityLevel());
        snapshot.setHideExample(p.getHideExample());
        return snapshot;
    }

    private Set<String> parseStringSet(String jsonOrNull) {
        if (!StringUtils.hasText(jsonOrNull)) {
            return Set.of();
        }
        try {
            List<String> arr = JSON.parseArray(jsonOrNull.trim(), String.class);
            if (arr == null) {
                return Set.of();
            }
            return new LinkedHashSet<>(arr);
        } catch (Exception e) {
            return Set.of();
        }
    }

    // parseStringList 已迁移到资产查询策略中
}
