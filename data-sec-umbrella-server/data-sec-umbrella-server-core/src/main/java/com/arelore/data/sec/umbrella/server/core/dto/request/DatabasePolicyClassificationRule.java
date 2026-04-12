package com.arelore.data.sec.umbrella.server.core.dto.request;

/**
 * 数据库策略中的一条分类规则（条件对象 + 条件类型 + 表达式 + 可选命中率阈值）。
 */
public class DatabasePolicyClassificationRule {

    private String conditionObject;
    private String conditionType;
    private String expression;

    /**
     * 命中率阈值（0–100，百分比）。仅当检测样例多于 1 条且本字段非空时生效：命中率须<strong>大于</strong>该值才判定本条分类规则通过。
     */
    private Integer ratio;

    public String getConditionObject() {
        return conditionObject;
    }

    public void setConditionObject(String conditionObject) {
        this.conditionObject = conditionObject;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Integer getRatio() {
        return ratio;
    }

    public void setRatio(Integer ratio) {
        this.ratio = ratio;
    }
}
