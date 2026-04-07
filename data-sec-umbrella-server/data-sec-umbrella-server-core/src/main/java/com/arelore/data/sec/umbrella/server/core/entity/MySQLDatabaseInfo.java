package com.arelore.data.sec.umbrella.server.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * MySQL数据库信息实体类
 */
@Data
@TableName("db_asset_mysql_database_info")
public class MySQLDatabaseInfo {

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
     * 数据库描述
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
     * 人工打标（IGNORE/FALSE_POSITIVE/SENSITIVE），null 表示未人工打标、沿用系统默认。
     */
    @TableField("manual_sensitive")
    private String manualReview;
}