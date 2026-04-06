package com.arelore.data.sec.umbrella.server.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * MySQL表信息实体类
 */
@Data
@TableName("db_asset_mysql_table_info")
public class MySQLTableInfo {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifyTime;

    /**
     * 修改人
     */
    private String modifier;

    /**
     * 实例（域名:端口）
     */
    private String instance;

    /**
     * 数据库名
     */
    @TableField("database_name")
    private String databaseName;

    /**
     * 表名
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 表描述
     */
    private String description;

    /**
     * 敏感等级
     */
    @TableField("sensitivity_level")
    private String sensitivityLevel;

    /**
     * 敏感标签（逗号分隔）
     */
    @TableField("sensitivity_tags")
    private String sensitivityTags;

    /**
     * AI敏感等级
     */
    @TableField("ai_sensitivity_level")
    private String aiSensitivityLevel;

    /**
     * AI敏感标签（逗号分隔）
     */
    @TableField("ai_sensitivity_tags")
    private String aiSensitivityTags;

    /**
     * 人审是否敏感
     */
    @TableField("manual_sensitive")
    private Boolean manualSensitive;

    /**
     * 列信息（JSON格式）
     */
    @TableField("column_info")
    private String columnInfo;

    /**
     * 列扫描信息（JSON格式）
     */
    @TableField("column_scan_info")
    private String columnScanInfo;

    /**
     * 列AI扫描信息（JSON格式）
     */
    @TableField("column_ai_scan_info")
    private String columnAiScanInfo;
}