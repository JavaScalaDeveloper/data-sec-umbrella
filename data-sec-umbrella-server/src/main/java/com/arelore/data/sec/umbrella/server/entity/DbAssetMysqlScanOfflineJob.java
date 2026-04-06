package com.arelore.data.sec.umbrella.server.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * MySQL 数据资产离线扫描任务
 */
@Data
@TableName("db_asset_mysql_scan_offline_job")
public class DbAssetMysqlScanOfflineJob {

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

    @TableField("task_description")
    private String taskDescription;

    @TableField("sample_count")
    private Integer sampleCount;

    /**
     * sequence / reverse / random
     */
    @TableField("sample_mode")
    private String sampleMode;

    @TableField("enable_sampling")
    private Integer enableSampling;

    @TableField("enable_ai_scan")
    private Integer enableAiScan;

    /**
     * manual / weekly / monthly
     */
    @TableField("scan_period")
    private String scanPeriod;

    @TableField("supported_tags")
    private String supportedTags;

    /**
     * all / instance
     */
    @TableField("scan_scope")
    private String scanScope;

    /**
     * full / incremental
     */
    @TableField("time_range_type")
    private String timeRangeType;

    /**
     * 0 停用 1 启用
     */
    @TableField("enabled_status")
    private Integer enabledStatus;
}
