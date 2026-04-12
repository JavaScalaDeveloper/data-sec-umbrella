package com.arelore.data.sec.umbrella.server.worker.ai;

import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineDatabaseScanDispatchPayload;

import java.util.Map;

/**
 * 按数据库引擎处理离线 AI 扫描：加载资产、执行 {@code checkAiRules}、回写与快照发布。
 * 与 {@link com.arelore.data.sec.umbrella.server.worker.scanner.AssetScanner} 分工不同：本接口只做 AI 链路。
 */
public interface OfflineAiAssetHandler {

    /**
     * 与 {@link com.arelore.data.sec.umbrella.server.worker.scanner.AssetScanner#databaseType()} 取值对齐，用于路由。
     */
    String databaseType();

    /**
     * 处理单条资产 AI 扫描。
     *
     * @return 未处理或资产不存在时可返回 {@code null}，由调用方视为跳过（不计 ai_fail）
     */
    AiAssetProcessOutcome processOne(OfflineDatabaseScanDispatchPayload payload, Map<String, Object> asset);
}
