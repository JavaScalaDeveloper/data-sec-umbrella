package com.arelore.data.sec.umbrella.server.core.entity.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * ClickHouse 表级资产（落主库表 db_asset_clickhouse_table_info）
 */
@Data
@TableName("db_asset_clickhouse_table_info")
public class ClickhouseTableInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Date createTime;

    private Date modifyTime;

    private String modifier;

    private String instance;

    @TableField("database_name")
    private String databaseName;

    @TableField("table_name")
    private String tableName;

    private String description;

    @TableField("sensitivity_level")
    private String sensitivityLevel;

    @TableField("sensitivity_tags")
    private String sensitivityTags;

    @TableField("ai_sensitivity_level")
    private String aiSensitivityLevel;

    @TableField("ai_sensitivity_tags")
    private String aiSensitivityTags;

    @TableField("manual_sensitive")
    private String manualReview;

    @TableField("column_info")
    private String columnInfo;

    @TableField("column_scan_info")
    private String columnScanInfo;

    @TableField("column_ai_scan_info")
    private String columnAiScanInfo;
}
