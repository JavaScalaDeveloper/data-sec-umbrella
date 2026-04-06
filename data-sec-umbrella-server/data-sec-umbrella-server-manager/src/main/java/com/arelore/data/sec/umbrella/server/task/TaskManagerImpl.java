package com.arelore.data.sec.umbrella.server.task;

import com.alibaba.fastjson2.JSON;
import com.arelore.data.sec.umbrella.server.constant.OfflineScanConstants;
import com.arelore.data.sec.umbrella.server.dto.messaging.OfflineMysqlScanDispatchPayload;
import com.arelore.data.sec.umbrella.server.dto.response.DatabasePolicyResponse;
import com.arelore.data.sec.umbrella.server.entity.DbAssetMysqlScanOfflineJob;
import com.arelore.data.sec.umbrella.server.entity.DbAssetMysqlScanOfflineJobInstance;
import com.arelore.data.sec.umbrella.server.mapper.DbAssetMysqlScanOfflineJobMapper;
import com.arelore.data.sec.umbrella.server.service.DatabasePolicyService;
import com.arelore.data.sec.umbrella.server.service.DbAssetMysqlScanOfflineJobInstanceService;
import com.arelore.data.sec.umbrella.server.task.asset.AssetPage;
import com.arelore.data.sec.umbrella.server.task.asset.AssetQueryStrategy;
import com.arelore.data.sec.umbrella.server.task.asset.MySQLTableAssetQueryStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
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

    private static final String RUN_WAITING = "waiting";
    private static final String RUN_RUNNING = "running";
    private static final String RUN_FAILED = "failed";

    private final DbAssetMysqlScanOfflineJobInstanceService jobInstanceService;
    private final DbAssetMysqlScanOfflineJobMapper offlineJobMapper;
    private final DatabasePolicyService databasePolicyService;
    private final MySQLTableAssetQueryStrategy mySQLAssetQueryStrategy;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void dispatchOfflineMysqlScan() {
        LambdaQueryWrapper<DbAssetMysqlScanOfflineJobInstance> qw = new LambdaQueryWrapper<>();
        qw.eq(DbAssetMysqlScanOfflineJobInstance::getRunStatus, RUN_WAITING);
        qw.orderByAsc(DbAssetMysqlScanOfflineJobInstance::getId);
        List<DbAssetMysqlScanOfflineJobInstance> waiting = jobInstanceService.list(qw);
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
        inst.setRunStatus(RUN_FAILED);
        inst.setExtendInfo(JSON.toJSONString(Map.of("reason", reason != null ? reason : "unknown")));
        jobInstanceService.updateById(inst);
    }

    private void dispatchOne(DbAssetMysqlScanOfflineJobInstance inst, List<DatabasePolicyResponse> allPolicies)
            throws Exception {
        LambdaQueryWrapper<DbAssetMysqlScanOfflineJob> w = new LambdaQueryWrapper<>();
        w.eq(DbAssetMysqlScanOfflineJob::getTaskName, inst.getTaskName());
        w.orderByDesc(DbAssetMysqlScanOfflineJob::getId);
        w.last("limit 1");
        DbAssetMysqlScanOfflineJob job = offlineJobMapper.selectOne(w);
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
        List<Map<String, Object>> policySnapshots = policies.stream().map(this::policyToMap).collect(Collectors.toList());

        while (submitted < expectedTotal) {
            AssetPage page = assetQueryStrategy.page(job, current, pageSize);
            if (page.getRecords() == null || page.getRecords().isEmpty()) {
                break;
            }
            for (Map<String, Object> asset : page.getRecords()) {
                OfflineMysqlScanDispatchPayload payload = new OfflineMysqlScanDispatchPayload();
                payload.setInstanceId(inst.getId());
                payload.setJobId(job.getId());
                payload.setTaskName(job.getTaskName());
                payload.setJobConfig(buildJobConfig(job));
                // 每个表资产一个 msg
                payload.setAssets(List.of(asset));
                payload.setPolicies(policySnapshots);

                String body = JSON.toJSONString(payload);
                rabbitTemplate.convertAndSend(
                        OfflineScanConstants.RABBIT_EXCHANGE,
                        OfflineScanConstants.RABBIT_ROUTING_KEY,
                        body);
                submitted++;
            }
            current++;
        }

        stringRedisTemplate.opsForValue().set(
                OfflineScanConstants.REDIS_KEY_INSTANCE_PREFIX + inst.getId(),
                String.valueOf(job.getId()),
                Duration.ofDays(7));
        stringRedisTemplate.opsForValue().set(
                OfflineScanConstants.REDIS_KEY_LAST_INSTANCE,
                String.valueOf(inst.getId()),
                Duration.ofDays(7));

        inst.setRunStatus(RUN_RUNNING);
        inst.setSubmittedTotal(submitted);
        if (job.getEnableAiScan() != null && job.getEnableAiScan() == 1) {
            inst.setAiSubmittedTotal(submitted);
        } else {
            inst.setAiSubmittedTotal(0);
        }
        inst.setExtendInfo(JSON.toJSONString(Map.of(
                "exchange", OfflineScanConstants.RABBIT_EXCHANGE,
                "routingKey", OfflineScanConstants.RABBIT_ROUTING_KEY)));
        jobInstanceService.updateById(inst);
        log.info("Dispatched offline scan instance {} job {}", inst.getId(), job.getId());
    }

    private Map<String, Object> buildJobConfig(DbAssetMysqlScanOfflineJob job) {
        Map<String, Object> m = new HashMap<>();
        m.put("sampleCount", job.getSampleCount());
        m.put("sampleMode", job.getSampleMode());
        m.put("enableSampling", job.getEnableSampling());
        m.put("enableAiScan", job.getEnableAiScan());
        m.put("scanPeriod", job.getScanPeriod());
        m.put("supportedTags", job.getSupportedTags());
        m.put("scanScope", job.getScanScope());
        m.put("scanInstanceIds", job.getScanInstanceIds());
        m.put("timeRangeType", job.getTimeRangeType());
        return m;
    }

    private Map<String, Object> policyToMap(DatabasePolicyResponse p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("policyCode", p.getPolicyCode());
        m.put("policyName", p.getPolicyName());
        m.put("classificationRules", p.getClassificationRules());
        m.put("ruleExpression", p.getRuleExpression());
        m.put("aiRule", p.getAiRule());
        m.put("databaseType", p.getDatabaseType());
        m.put("sensitivityLevel", p.getSensitivityLevel());
        m.put("hideExample", p.getHideExample());
        return m;
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
