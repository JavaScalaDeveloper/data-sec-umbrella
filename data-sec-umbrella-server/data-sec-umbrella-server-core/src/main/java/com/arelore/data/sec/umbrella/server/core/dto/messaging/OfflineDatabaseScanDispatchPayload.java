package com.arelore.data.sec.umbrella.server.core.dto.messaging;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 投递到 MQ 的离线数据库扫描分发消息体（JSON 序列化）。
 * <p>
 * 与具体引擎解耦：{@link #engine} 标识主数据源类型（如 MySQL、ClickHouse），
 * 单条资产仍可在 {@code assets[].databaseType} 中覆盖，便于多引擎混扫扩展。
 */
@Data
public class OfflineDatabaseScanDispatchPayload {

    /** 逻辑引擎 / 数据源产品类型，如 MySQL、ClickHouse */
    private String engine;
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
