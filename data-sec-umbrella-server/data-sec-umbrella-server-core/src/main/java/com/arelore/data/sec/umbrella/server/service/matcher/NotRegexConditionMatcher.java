package com.arelore.data.sec.umbrella.server.service.matcher;

/**
 * 非正则匹配条件匹配器
 */
public class NotRegexConditionMatcher implements ConditionMatcher {
    @Override
    public String getConditionType() {
        return "非正则匹配";
    }

    @Override
    public boolean match(String value, String expression) {
        try {
            return !value.matches(expression);
        } catch (Exception e) {
            return false;
        }
    }
}
