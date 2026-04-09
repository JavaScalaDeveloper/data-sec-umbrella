package com.arelore.data.sec.umbrella.server.worker.task;

import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineMysqlScanDispatchPayload;

/**
 * AI 专用离线扫描 Worker。
 *
 * @author 黄佳豪
 */
public interface AiTaskWorker {
    /**
     * 处理一条 AI 扫描消息。
     *
     * @param payload 扫描任务消息
     */
    void process(OfflineMysqlScanDispatchPayload payload);
}
