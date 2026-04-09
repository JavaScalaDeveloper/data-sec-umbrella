package com.arelore.data.sec.umbrella.server.worker.task;

import com.alibaba.fastjson2.JSON;
import com.arelore.data.sec.umbrella.server.core.constant.OfflineScanConstants;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineMysqlScanDispatchPayload;
import com.arelore.data.sec.umbrella.server.core.entity.DbAssetMysqlScanOfflineJobInstance;
import com.arelore.data.sec.umbrella.server.core.enums.OfflineJobRunStatusEnum;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetMysqlScanOfflineJobInstanceService;
import com.arelore.data.sec.umbrella.server.worker.executor.TaskWorkerExecutorManager;
import com.arelore.data.sec.umbrella.server.worker.scanner.AssetScanResult;
import com.arelore.data.sec.umbrella.server.worker.scanner.AssetScanner;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
/**
 * Worker 任务执行实现，负责扫描路由、分布式计数与实例状态同步。
 *
 * @author 黄佳豪
 */
public class TaskWorkerImpl implements TaskWorker {

    @Autowired
    private TaskWorkerExecutorManager executorManager;

    @Autowired
    private List<AssetScanner> scanners;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private DbAssetMysqlScanOfflineJobInstanceService jobInstanceService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final Map<String, AssetScanner> scannerMap = new ConcurrentHashMap<>();

    @PostConstruct
    /**
     * 启动后构建数据库类型到扫描器的映射。
     */
    public void initScanners() {
        for (AssetScanner scanner : scanners) {
            this.scannerMap.put(scanner.databaseType(), scanner);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(OfflineMysqlScanDispatchPayload payload) {
        if (payload == null) {
            return;
        }
        Long instanceId = payload.getInstanceId();
        List<Map<String, Object>> assets = payload.getAssets();
        if (assets == null || assets.isEmpty()) {
            return;
        }
        for (Map<String, Object> asset : assets) {
            String type = String.valueOf(asset.getOrDefault("databaseType", "MySQL"));
            AssetScanner scanner = scannerMap.getOrDefault(type, scannerMap.get("MySQL"));
            try {
                if (scanner == null) {
                    incrementRedis(instanceId, "fail");
                    continue;
                }
                AssetScanResult result = scanner.scan(payload, asset);
                incrementRedis(instanceId, "success");
                if (result != null && result.sensitive()) {
                    incrementRedis(instanceId, "sensitive");
                }
                dispatchAiScanIfNeeded(payload, asset, result);
            } catch (Exception ex) {
                incrementRedis(instanceId, "fail");
            }
        }
        syncInstanceProgress(instanceId);
    }

    private void dispatchAiScanIfNeeded(OfflineMysqlScanDispatchPayload payload, Map<String, Object> asset, AssetScanResult result) {
        if (payload == null || payload.getJobConfig() == null || payload.getJobConfig().getEnableAiScan() == null
                || payload.getJobConfig().getEnableAiScan() != 1) {
            return;
        }
        Map<String, Object> aiAsset = new ConcurrentHashMap<>(asset);
        if (result != null) {
            aiAsset.put("assetId", result.assetId());
            aiAsset.put("databaseType", result.databaseType());
            aiAsset.put("columnScanInfo", result.columnScanInfo());
            aiAsset.put("columnSamples", result.samples());
        }
        OfflineMysqlScanDispatchPayload aiPayload = new OfflineMysqlScanDispatchPayload();
        aiPayload.setInstanceId(payload.getInstanceId());
        aiPayload.setJobId(payload.getJobId());
        aiPayload.setTaskName(payload.getTaskName());
        aiPayload.setDispatchVersion(payload.getDispatchVersion());
        aiPayload.setJobConfig(payload.getJobConfig());
        aiPayload.setPolicies(payload.getPolicies());
        aiPayload.setAssets(List.of(aiAsset));
        rabbitTemplate.convertAndSend(
                OfflineScanConstants.RABBIT_EXCHANGE,
                OfflineScanConstants.RABBIT_AI_ROUTING_KEY,
                JSON.toJSONString(aiPayload)
        );
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void updatePool(int coreSize, int maxSize, int queueCapacity, int keepAliveSeconds) {
        executorManager.updatePool(coreSize, maxSize, queueCapacity, keepAliveSeconds);
    }

    private void incrementRedis(Long instanceId, String metric) {
        if (instanceId == null) {
            return;
        }
        String key = buildMetricKey(instanceId, metric);
        stringRedisTemplate.opsForValue().increment(key);
    }

    private long getRedisCount(Long instanceId, String metric) {
        String value = stringRedisTemplate.opsForValue().get(buildMetricKey(instanceId, metric));
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private String buildMetricKey(Long instanceId, String metric) {
        return "offline-scan:instance:" + instanceId + ":" + metric;
    }

    private void syncInstanceProgress(Long instanceId) {
        if (instanceId == null) {
            return;
        }
        DbAssetMysqlScanOfflineJobInstance inst = jobInstanceService.getById(instanceId);
        if (inst == null) {
            return;
        }
        long success = getRedisCount(instanceId, "success");
        long fail = getRedisCount(instanceId, "fail");
        long sensitive = getRedisCount(instanceId, "sensitive");
        int submitted = inst.getSubmittedTotal() == null ? 0 : inst.getSubmittedTotal();
        long cappedSuccess = submitted > 0 ? Math.min(success, submitted) : success;
        long cappedFail = submitted > 0 ? Math.min(fail, Math.max(0, submitted - cappedSuccess)) : fail;
        inst.setSuccessCount((int) Math.min(Integer.MAX_VALUE, cappedSuccess));
        inst.setFailCount((int) Math.min(Integer.MAX_VALUE, cappedFail));
        inst.setSensitiveCount((int) Math.min(Integer.MAX_VALUE, sensitive));

        if (submitted > 0 && cappedSuccess + cappedFail >= submitted) {
            if (cappedSuccess > 0) {
                inst.setRunStatus(OfflineJobRunStatusEnum.COMPLETED.getValue());
            } else {
                inst.setRunStatus(OfflineJobRunStatusEnum.FAILED.getValue());
            }
        } else {
            inst.setRunStatus(OfflineJobRunStatusEnum.RUNNING.getValue());
        }
        jobInstanceService.updateById(inst);
    }
}

