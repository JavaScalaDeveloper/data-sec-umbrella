package com.arelore.data.sec.umbrella.server.service.matcher;

/**
 * 以...结尾条件匹配器
 */
public class EndsWithConditionMatcher implements ConditionMatcher {
    @Override
    public String getConditionType() {
        return "以...结尾";
    }

    @Override
    public boolean match(String value, String expression) {
        return value.endsWith(expression);
    }
}