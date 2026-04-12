package com.arelore.data.sec.umbrella.server.worker.task;

import com.alibaba.fastjson2.JSON;
import com.arelore.data.sec.umbrella.server.core.constant.OfflineScanConstants;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineDatabaseScanDispatchPayload;
import com.arelore.data.sec.umbrella.server.worker.scanner.AssetScanResult;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则扫描完成后，按需向 AI 队列投递消息（与 {@link TaskWorkerImpl} 中规则处理解耦）。
 *
 * @author 黄佳豪
 */
@Service
public class OfflineAiScanDispatchService {

    private final RabbitTemplate rabbitTemplate;

    public OfflineAiScanDispatchService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 任务开启 AI 扫描时，在规则扫描成功后投递 AI 子任务（携带列样例，不在此处做 LLM 调用）。
     */
    public void dispatchIfNeeded(OfflineDatabaseScanDispatchPayload payload, Map<String, Object> asset, AssetScanResult result) {
        if (payload == null || payload.getJobConfig() == null || payload.getJobConfig().getEnableAiScan() == null
                || payload.getJobConfig().getEnableAiScan() != 1) {
            return;
        }
        Map<String, Object> aiAsset = new ConcurrentHashMap<>(asset);
        if (result != null) {
            aiAsset.put("assetId", result.assetId());
            aiAsset.put("databaseType", result.databaseType());
            aiAsset.put("columnSamples", result.samples());
        }
        OfflineDatabaseScanDispatchPayload aiPayload = new OfflineDatabaseScanDispatchPayload();
        aiPayload.setEngine(payload.getEngine());
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
}
