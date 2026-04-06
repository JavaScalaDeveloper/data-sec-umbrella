package com.arelore.data.sec.umbrella.server.core.service.matcher;

/**
 * 等于条件匹配器
 */
public class EqualsConditionMatcher implements ConditionMatcher {
    @Override
    public String getConditionType() {
        return "等于";
    }

    @Override
    public boolean match(String value, String expression) {
        return value.equals(expression);
    }
}