package com.arelore.data.sec.umbrella.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API策略实体类
 */
@Data
@TableName("api_policy")
public class ApiPolicy {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 策略编码
     */
    @TableField("policy_code")
    private String policyCode;

    /**
     * 策略名称
     */
    @TableField("policy_name")
    private String policyName;

    /**
     * 策略描述
     */
    @TableField("description")
    private String description;

    /**
     * 策略状态（1-启用，0-禁用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 分类规则（JSON格式）
     */
    @TableField("classification_rules")
    private String classificationRules;

    /**
     * 规则表达式（JSON格式）
     */
    @TableField("rule_expressions")
    private String ruleExpressions;

    /**
     * AI规则
     */
    @TableField("ai_rules")
    private String aiRules;

    /**
     * 验证数据（JSON格式）
     */
    @TableField("validation_data")
    private String validationData;

    /**
     * 隐藏样例（1-是，0-否）
     */
    @TableField("hide_example")
    private Integer hideExample;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 创建者
     */
    @TableField("creator")
    private String creator;

    /**
     * 更新者
     */
    @TableField("updater")
    private String updater;
}