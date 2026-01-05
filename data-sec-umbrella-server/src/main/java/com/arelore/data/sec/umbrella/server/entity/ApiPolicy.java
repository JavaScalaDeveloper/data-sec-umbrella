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
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField("modify_time")
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
     * 敏感等级 1-5，越高代表越敏感
     */
    @TableField("sensitivity_level")
    private Integer sensitivityLevel;

    /**
     * 隐藏样例（1-是，0-否）
     */
    @TableField("hide_example")
    private Integer hideExample;

    /**
     * 分类规则（JSON格式）
     */
    @TableField("classification_rules")
    private String classificationRules;

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
}