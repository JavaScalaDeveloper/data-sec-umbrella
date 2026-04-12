package com.arelore.data.sec.umbrella.server.core.dto.request;

import java.util.List;

/**
 * 数据库策略「规则检测」请求：分类规则、组合表达式、AI 规则及资产样例。
 */
public class DatabasePolicyRuleDetectionRequest {

    private List<DatabasePolicyClassificationRule> classificationRules;
    private String ruleExpression;
    private String aiRule;

    /** 参与检测的资产样例列表（库/表/列及列值等）。 */
    private List<DatabasePolicyAssetSample> samples;

    private String databaseType;

    public List<DatabasePolicyClassificationRule> getClassificationRules() {
        return classificationRules;
    }

    public void setClassificationRules(List<DatabasePolicyClassificationRule> classificationRules) {
        this.classificationRules = classificationRules;
    }

    public String getRuleExpression() {
        return ruleExpression;
    }

    public void setRuleExpression(String ruleExpression) {
        this.ruleExpression = ruleExpression;
    }

    public String getAiRule() {
        return aiRule;
    }

    public void setAiRule(String aiRule) {
        this.aiRule = aiRule;
    }

    public List<DatabasePolicyAssetSample> getSamples() {
        return samples;
    }

    public void setSamples(List<DatabasePolicyAssetSample> samples) {
        this.samples = samples;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }
}
