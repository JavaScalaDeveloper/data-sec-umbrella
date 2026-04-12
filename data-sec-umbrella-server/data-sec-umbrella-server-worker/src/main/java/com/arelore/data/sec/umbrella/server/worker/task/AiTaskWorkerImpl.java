package com.arelore.data.sec.umbrella.server.worker.task;

import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineDatabaseScanDispatchPayload;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.DbAssetMysqlScanOfflineJobInstance;
import com.arelore.data.sec.umbrella.server.core.enums.OfflineJobRunStatusEnum;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetMysqlScanOfflineJobInstanceService;
import com.arelore.data.sec.umbrella.server.worker.ai.AiAssetProcessOutcome;
import com.arelore.data.sec.umbrella.server.worker.ai.OfflineAiAssetHandler;
import com.arelore.data.sec.umbrella.server.worker.ai.OfflineAiScanSupport;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 专用 Worker：按资产 {@code databaseType}（或任务 {@code engine}）路由到对应 {@link OfflineAiAssetHandler}。
 */
@Slf4j
@Service("offlineAiScanTaskProcessor")
public class AiTaskWorkerImpl implements OfflineScanTaskProcessor {

    private final List<OfflineAiAssetHandler> aiAssetHandlers;
    private final StringRedisTemplate stringRedisTemplate;
    private final DbAssetMysqlScanOfflineJobInstanceService jobInstanceService;

    private final Map<String, OfflineAiAssetHandler> handlerByType = new ConcurrentHashMap<>();

    public AiTaskWorkerImpl(List<OfflineAiAssetHandler> aiAssetHandlers,
                            StringRedisTemplate stringRedisTemplate,
                            DbAssetMysqlScanOfflineJobInstanceService jobInstanceService) {
        this.aiAssetHandlers = aiAssetHandlers;
        this.stringRedisTemplate = stringRedisTemplate;
        this.jobInstanceService = jobInstanceService;
    }

    @PostConstruct
    public void registerAiHandlers() {
        for (OfflineAiAssetHandler h : aiAssetHandlers) {
            String key = OfflineAiScanSupport.normalizeDatabaseType(h.databaseType());
            handlerByType.put(key, h);
        }
    }

    @Override
    public void process(OfflineDatabaseScanDispatchPayload payload) {
        if (payload == null || payload.getAssets() == null || payload.getAssets().isEmpty()) {
            return;
        }
        Long instanceId = payload.getInstanceId();
        for (Map<String, Object> asset : payload.getAssets()) {
            try {
                boolean llmInvocationFailed = processOne(payload, asset);
                incrementRedis(instanceId, llmInvocationFailed ? "ai_fail" : "ai_success");
            } catch (Exception ex) {
                log.error("ai processOne failed instanceId={}", instanceId, ex);
                incrementRedis(instanceId, "ai_fail");
            }
        }
        syncAiInstanceProgress(instanceId);
    }

    /**
     * @return true 表示 LLM 基础设施类失败（与「模型判定未命中」区分），用于统计 ai_fail
     */
    private boolean processOne(OfflineDatabaseScanDispatchPayload payload, Map<String, Object> asset) {
        OfflineAiAssetHandler handler = selectHandler(asset, payload);
        if (handler == null) {
            log.error("无可用 OfflineAiAssetHandler，databaseType={}", OfflineAiScanSupport.resolveDatabaseType(asset, payload));
            return true;
        }
        AiAssetProcessOutcome outcome = handler.processOne(payload, asset);
        if (outcome == null) {
            return false;
        }
        if (outcome.sensitiveHit()) {
            incrementRedis(payload.getInstanceId(), "ai_sensitive");
        }
        return outcome.llmInvocationFailed();
    }

    private OfflineAiAssetHandler selectHandler(Map<String, Object> asset, OfflineDatabaseScanDispatchPayload payload) {
        String db = OfflineAiScanSupport.resolveDatabaseType(asset, payload);
        OfflineAiAssetHandler h = handlerByType.get(db);
        if (h != null) {
            return h;
        }
        log.warn("未注册 AI 引擎 [{}]，回退 MySQL", db);
        return handlerByType.get("MySQL");
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
            int mainSubmitted = inst.getSubmittedTotal() == null ? 0 : inst.getSubmittedTotal();
            int mainDone = (inst.getSuccessCount() == null ? 0 : inst.getSuccessCount()) + (inst.getFailCount() == null ? 0 : inst.getFailCount());
            if (mainSubmitted > 0 && mainDone >= mainSubmitted && submitted > 0 && cappedSuccess + cappedFail >= submitted) {
                inst.setRunStatus(OfflineJobRunStatusEnum.COMPLETED.getValue());
            }
        }
        jobInstanceService.updateById(inst);
    }
}
