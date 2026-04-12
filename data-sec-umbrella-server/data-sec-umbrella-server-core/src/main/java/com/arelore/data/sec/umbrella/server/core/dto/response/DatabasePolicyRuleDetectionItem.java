package com.arelore.data.sec.umbrella.server.core.dto.response;

/**
 * 单条分类规则在规则检测中的命中结果。
 */
public class DatabasePolicyRuleDetectionItem {

    private int rule;
    private boolean matched;
    private String detail;

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
