package com.arelore.data.sec.umbrella.server.core.service.matcher;

/**
 * 不以...结尾条件匹配器
 */
public class NotEndsWithConditionMatcher implements ConditionMatcher {
    @Override
    public String getConditionType() {
        return "不以...结尾";
    }

    @Override
    public boolean match(String value, String expression) {
        return !value.endsWith(expression);
    }
}