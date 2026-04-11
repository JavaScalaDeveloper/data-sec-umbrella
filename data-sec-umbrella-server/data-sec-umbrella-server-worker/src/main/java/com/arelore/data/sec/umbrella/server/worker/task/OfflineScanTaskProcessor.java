package com.arelore.data.sec.umbrella.server.worker.task;

import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineDatabaseScanDispatchPayload;

/**
 * 离线扫描任务处理入口（规则扫描与 AI 扫描共用同一契约，由不同实现类承载）。
 *
 * @author 黄佳豪
 */
public interface OfflineScanTaskProcessor {

    /**
     * 处理一条离线扫描消息。
     *
     * @param payload MQ 下发的扫描任务载荷
     */
    void process(OfflineDatabaseScanDispatchPayload payload);

    /**
     * 动态更新规则扫描线程池参数（仅规则 Worker 实现；AI Worker 可为空操作）。
     *
     * @param coreSize 核心线程数
     * @param maxSize 最大线程数
     * @param queueCapacity 队列容量（当前实现保留参数，队列容量本身不可热更新）
     * @param keepAliveSeconds 非核心线程存活时间（秒）
     */
    default void updatePool(int coreSize, int maxSize, int queueCapacity, int keepAliveSeconds) {
    }
}
