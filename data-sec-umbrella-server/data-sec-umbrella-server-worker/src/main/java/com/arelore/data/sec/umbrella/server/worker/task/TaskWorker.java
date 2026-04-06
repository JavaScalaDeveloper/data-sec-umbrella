package com.arelore.data.sec.umbrella.server.worker.task;

import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineMysqlScanDispatchPayload;

/**
 * 离线扫描 Worker 执行入口。
 *
 * @author 黄佳豪
 */
public interface TaskWorker {
    /**
     * 处理一条离线扫描消息。
     *
     * @param payload MQ 下发的扫描任务载荷
     */
    void process(OfflineMysqlScanDispatchPayload payload);

    /**
     * 动态更新扫描线程池参数。
     *
     * @param coreSize 核心线程数
     * @param maxSize 最大线程数
     * @param queueCapacity 队列容量（当前实现保留参数，队列容量本身不可热更新）
     * @param keepAliveSeconds 非核心线程存活时间（秒）
     */
    void updatePool(int coreSize, int maxSize, int queueCapacity, int keepAliveSeconds);
}

