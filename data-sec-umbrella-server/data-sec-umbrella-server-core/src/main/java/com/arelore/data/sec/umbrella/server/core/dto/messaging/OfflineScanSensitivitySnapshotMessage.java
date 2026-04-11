package com.arelore.data.sec.umbrella.server.core.dto.messaging;

import lombok.Data;

import java.util.List;

/**
 * 敏感扫描快照行（表级或字段级共用），经 MQ 投递给下游。
 * <ul>
 *   <li>表级：{@code uniqueKey} = 实例,库名,表名（逗号拼接）；{@link #columnDetails} 为 JSON 数组字符串</li>
 *   <li>字段级：{@code uniqueKey} 含列名；{@link #samples}、{@link #sensitiveSamples} 为列级样例</li>
 * </ul>
 */
@Data
public class OfflineScanSensitivitySnapshotMessage {

    /** 任务实例 ID，供管理端与 ClickHouse 按实例查询 */
    private Long instanceId;
    private Long jobId;
    private Long dispatchVersion;
    private String taskName;
    /** RULE / AI */
    private String scanKind;
    private String engine;
    /** 毫秒时间戳，下游写入 ClickHouse 时转为 DateTime */
    private Long eventTime;

    /** 业务唯一键，见类注释 */
    private String uniqueKey;
    /** 敏感等级 */
    private String sensitivityLevel;
    /** 敏感标签列表 */
    private List<String> sensitivityTags;

    /**
     * 表级专用：本表所有列详情，JSON 数组字符串。
     * 元素字段：column_name、samples、sensitive_samples、sensitivity_level、sensitivity_tags
     */
    private String columnDetails;

    /** 字段级专用：该列样例列表 */
    private List<String> samples;
    /** 字段级专用：该列敏感样例列表 */
    private List<String> sensitiveSamples;
}
