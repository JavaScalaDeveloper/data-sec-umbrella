package com.arelore.data.sec.umbrella.server.dto.response;

import java.util.List;

/**
 * 测试规则响应DTO
 */
public class DatabasePolicyTestRulesResponse {
    /**
     * 规则验证是否通过
     */
    private boolean rulePassed;

    /**
     * AI规则验证是否通过
     */
    private boolean aiPassed;

    /**
     * 规则命中详情列表
     */
    private List<RuleDetail> ruleDetails;

    /**
     * AI分析详情
     */
    private String aiDetail;

    /**
     * 规则命中详情
     */
    public static class RuleDetail {
        /**
         * 规则编号
         */
        private int rule;

        /**
         * 是否命中
         */
        private boolean matched;

        /**
         * 详情描述
         */
        private String detail;

        // Getters and Setters
        public int getRule() {
            return rule;
        }

        public void setRule(int rule) {
            this.rule = rule;
        }

        public boolean isMatched() {
            return matched;
        }

        public void setMatched(boolean matched) {
            this.matched = matched;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }
    }

    // Getters and Setters
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

    public List<RuleDetail> getRuleDetails() {
        return ruleDetails;
    }

    public void setRuleDetails(List<RuleDetail> ruleDetails) {
        this.ruleDetails = ruleDetails;
    }

    public String getAiDetail() {
        return aiDetail;
    }

    public void setAiDetail(String aiDetail) {
        this.aiDetail = aiDetail;
    }
}