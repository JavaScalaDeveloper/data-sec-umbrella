package com.arelore.data.sec.umbrella.server.entity;

import com.baomidou.mybatisplus.annotation.*;
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
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(value = "modify_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime modifyTime;

    /**
     * 创建人
     */
    @TableField("creator")
    private String creator;

    /**
     * 修改人
     */
    @TableField("modifier")
    private String modifier;

    /**
     * 策略code
     */
    @TableField("policy_code")
    private String policyCode;

    /**
     * 策略名
     */
    @TableField("policy_name")
    private String policyName;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 敏感等级 1-5，越高代表越敏感
     */
    @TableField("sensitivity_level")
    private Integer sensitivityLevel;

    /**
     * 隐藏样例 0-否 1-是
     */
    @TableField("hide_example")
    private Integer hideExample;

    /**
     * 规则表达式
     */
    @TableField("rule_expression")
    private String ruleExpression;

    /**
     * AI规则
     */
    @TableField("ai_rule")
    private String aiRule;

    /**
     * 分类规则
     */
    @TableField("classification_rules")
    private String classificationRules;
}