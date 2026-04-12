package com.arelore.data.sec.umbrella.server.worker.ai;

/**
 * 单条资产 AI 扫描处理结果（供 Worker 统计 Redis / 实例进度）。
 *
 * @param llmInvocationFailed LLM 基础设施类失败
 * @param sensitiveHit       是否检出敏感（用于 ai_sensitive 计数）
 */
public record AiAssetProcessOutcome(boolean llmInvocationFailed, boolean sensitiveHit) {
}
