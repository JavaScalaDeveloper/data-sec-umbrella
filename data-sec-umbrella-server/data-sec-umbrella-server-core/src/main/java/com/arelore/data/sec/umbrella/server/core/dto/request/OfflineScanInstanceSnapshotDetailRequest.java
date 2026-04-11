package com.arelore.data.sec.umbrella.server.core.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 管理端按任务实例查询 ClickHouse 快照。
 * <p>
 * {@link #scanKind} 必填（RULE / AI）；engine 由服务端根据实例解析，无需前端传入。
 */
@Data
public class OfflineScanInstanceSnapshotDetailRequest {

    private Long id;
    /** RULE 或 AI，与 MQ 消息中 scanKind 一致 */
    private String scanKind;

    /** 唯一键模糊：子串匹配（不区分大小写） */
    private String uniqueKeyContains;

    /** 敏感等级多选，命中 sensitivity_level IN (...) */
    private List<String> sensitivityLevels;

    /** 敏感标签模糊：在任一标签上做子串匹配（不区分大小写） */
    private String sensitivityTagsContains;
}
