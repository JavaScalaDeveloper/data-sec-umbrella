package com.arelore.data.sec.umbrella.server.core.dto.messaging;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 投递到 MQ 的离线扫描分发消息体（JSON 序列化）
 */
@Data
public class OfflineMysqlScanDispatchPayload {

    private Long instanceId;
    private Long jobId;
    private String taskName;
    /** 分发版本号（时间戳） */
    private Long dispatchVersion;
    /** 任务配置快照 */
    private OfflineJobConfigSnapshot jobConfig;
    /** 待扫描库资产快照 */
    private List<Map<String, Object>> assets;
    /** 适用策略快照 */
    private List<OfflinePolicySnapshot> policies;
}
