package com.arelore.data.sec.umbrella.server.service.matcher;

/**
 * 不以...开头条件匹配器
 */
public class NotStartsWithConditionMatcher implements ConditionMatcher {
    @Override
    public String getConditionType() {
        return "不以...开头";
    }

    @Override
    public boolean match(String value, String expression) {
        return !value.startsWith(expression);
    }
}