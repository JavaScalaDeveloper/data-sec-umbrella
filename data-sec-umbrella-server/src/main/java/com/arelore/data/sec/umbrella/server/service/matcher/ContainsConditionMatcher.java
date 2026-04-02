package com.arelore.data.sec.umbrella.server.service.matcher;

/**
 * 包含条件匹配器
 */
public class ContainsConditionMatcher implements ConditionMatcher {
    @Override
    public String getConditionType() {
        return "包含";
    }

    @Override
    public boolean match(String value, String expression) {
        return value.contains(expression);
    }
}