package com.arelore.data.sec.umbrella.server.core.dto.response;

import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyAssetSample;

import java.util.List;

/**
 * 数据库策略「规则检测」响应。
 */
public class DatabasePolicyRuleDetectionResponse {

    private boolean rulePassed;
    private boolean aiPassed;
    private List<DatabasePolicyRuleDetectionItem> ruleDetails;
    private String aiDetail;
    /** AI 判定为命中时返回的敏感样例（与请求中样例结构一致）。 */
    private List<DatabasePolicyAssetSample> aiSensitiveSamples;

    public boolean isRulePassed() {
        return rulePassed;
    }

    public void setRulePassed(boolean rulePassed) {
        this.rulePassed = rulePassed;
    }

    public boolean isAiPassed() {
        return aiPassed;
    }

    public void setAiPassed(boolean aiPassed) {
        this.aiPassed = aiPassed;
    }

    public List<DatabasePolicyRuleDetectionItem> getRuleDetails() {
        return ruleDetails;
    }

    public void setRuleDetails(List<DatabasePolicyRuleDetectionItem> ruleDetails) {
        this.ruleDetails = ruleDetails;
    }

    public String getAiDetail() {
        return aiDetail;
    }

    public void setAiDetail(String aiDetail) {
        this.aiDetail = aiDetail;
    }

    public List<DatabasePolicyAssetSample> getAiSensitiveSamples() {
        return aiSensitiveSamples;
    }

    public void setAiSensitiveSamples(List<DatabasePolicyAssetSample> aiSensitiveSamples) {
        this.aiSensitiveSamples = aiSensitiveSamples;
    }
}
