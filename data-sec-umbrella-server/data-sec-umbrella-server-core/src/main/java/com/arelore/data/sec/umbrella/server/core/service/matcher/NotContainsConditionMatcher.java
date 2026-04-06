package com.arelore.data.sec.umbrella.server.core.service.matcher;

/**
 * 不包含条件匹配器
 */
public class NotContainsConditionMatcher implements ConditionMatcher {
    @Override
    public String getConditionType() {
        return "不包含";
    }

    @Override
    public boolean match(String value, String expression) {
        return !value.contains(expression);
    }
}