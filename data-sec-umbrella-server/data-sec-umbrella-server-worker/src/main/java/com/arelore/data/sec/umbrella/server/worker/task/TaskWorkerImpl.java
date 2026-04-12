package com.arelore.data.sec.umbrella.server.worker.task;

import com.alibaba.fastjson2.JSON;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineDatabaseScanDispatchPayload;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineScanSensitivitySnapshotMessage;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineScanSnapshotUniqueKey;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.DbAssetMysqlScanOfflineJobInstance;
import com.arelore.data.sec.umbrella.server.core.enums.OfflineJobRunStatusEnum;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetMysqlScanOfflineJobInstanceService;
import com.arelore.data.sec.umbrella.server.worker.ai.OfflineAiScanSupport;
import com.arelore.data.sec.umbrella.server.worker.config.TaskWorkerExecutorBeans;
import com.arelore.data.sec.umbrella.server.worker.executor.TaskWorkerExecutorManager;
import com.arelore.data.sec.umbrella.server.worker.mq.OfflineScanSnapshotPublisher;
import com.arelore.data.sec.umbrella.server.worker.scanner.AssetScanResult;
import com.arelore.data.sec.umbrella.server.worker.scanner.AssetScanner;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Service("offlineRuleScanTaskProcessor")
/**
 * Worker 任务执行实现，负责扫描路由、分布式计数与实例状态同步。
 *
 * @author 黄佳豪
 */
public class TaskWorkerImpl implements OfflineScanTaskProcessor {

    @Autowired
    @Qualifier(TaskWorkerExecutorBeans.RULE_SCAN_EXECUTOR)
    private TaskWorkerExecutorManager executorManager;

    @Autowired
    private List<AssetScanner> scanners;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private DbAssetMysqlScanOfflineJobInstanceService jobInstanceService;

    @Autowired
    private OfflineScanSnapshotPublisher offlineScanSnapshotPublisher;

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
    public void process(OfflineDatabaseScanDispatchPayload payload) {
        if (payload == null) {
            return;
        }
        Long instanceId = payload.getInstanceId();
        List<Map<String, Object>> assets = payload.getAssets();
        if (assets == null || assets.isEmpty()) {
            return;
        }
        for (Map<String, Object> asset : assets) {
            String type = OfflineAiScanSupport.resolveDatabaseType(asset, payload);
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
                publishRuleScanSnapshot(payload, asset, result);
            } catch (Exception ex) {
                incrementRedis(instanceId, "fail");
            }
        }
        syncInstanceProgress(instanceId);
    }

    private void publishRuleScanSnapshot(OfflineDatabaseScanDispatchPayload payload, Map<String, Object> asset, AssetScanResult result) {
        if (payload == null || result == null) {
            return;
        }
        String dataInstance = str(asset.get("instance"));
        String databaseName = str(asset.get("databaseName"));
        String tableName = str(asset.get("tableName"));
        String engine = payload.getEngine();
        if (!StringUtils.hasText(engine)) {
            engine = OfflineAiScanSupport.resolveDatabaseType(asset, payload);
        }
        String tableKey = OfflineScanSnapshotUniqueKey.tableRowKey(dataInstance, databaseName, tableName);
        OfflineScanSensitivitySnapshotMessage tableMsg = sensitivityRow(
                payload,
                "RULE",
                engine,
                tableKey,
                result.maxLevel() == null ? "0" : String.valueOf(result.maxLevel()),
                result.tags() == null ? List.of() : result.tags()
        );
        tableMsg.setColumnDetails(buildColumnDetailsJson(result.columnScanInfo()));
        offlineScanSnapshotPublisher.publishTableSnapshot(tableMsg);
        List<AssetScanResult.ColumnScanInfoItem> columns = result.columnScanInfo();
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
            OfflineScanSensitivitySnapshotMessage colMsg = sensitivityRow(payload, "RULE", engine, colKey, colLevel, colTags);
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

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value);
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

