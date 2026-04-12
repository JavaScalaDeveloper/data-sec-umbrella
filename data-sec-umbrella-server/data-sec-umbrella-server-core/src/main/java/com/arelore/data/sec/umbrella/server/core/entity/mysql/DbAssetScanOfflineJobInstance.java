package com.arelore.data.sec.umbrella.server.core.entity.mysql;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 离线扫描任务执行实例
 */
@Data
@TableName("db_asset_scan_offline_job_instance")
public class DbAssetScanOfflineJobInstance {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date createTime;

    @TableField(value = "modify_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date modifyTime;

    @TableField("creator")
    private String creator;

    @TableField("modifier")
    private String modifier;

    @TableField("task_name")
    private String taskName;

    /**
     * waiting / running / stopped / completed / failed
     */
    @TableField("run_status")
    private String runStatus;

    @TableField("success_count")
    private Integer successCount;

    @TableField("fail_count")
    private Integer failCount;

    @TableField("sensitive_count")
    private Integer sensitiveCount;

    /**
     * 应扫描总数（表资产总数）
     */
    @TableField("expected_total")
    private Integer expectedTotal;

    /**
     * 已提交到 MQ 的总数
     */
    @TableField("submitted_total")
    private Integer submittedTotal;

    @TableField("ai_success_count")
    private Integer aiSuccessCount;

    @TableField("ai_fail_count")
    private Integer aiFailCount;

    @TableField("ai_sensitive_count")
    private Integer aiSensitiveCount;

    /**
     * AI 应扫描总数
     */
    @TableField("ai_expected_total")
    private Integer aiExpectedTotal;

    /**
     * AI 已提交到 MQ 的总数
     */
    @TableField("ai_submitted_total")
    private Integer aiSubmittedTotal;

    @TableField("extend_info")
    private String extendInfo;

    /**
     * 与关联任务一致：MySQL / Clickhouse
     */
    @TableField("database_type")
    private String databaseType;
}
