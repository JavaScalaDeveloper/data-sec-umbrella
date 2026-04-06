package com.arelore.data.sec.umbrella.server.core.service.matcher;

/**
 * 以...开头条件匹配器
 */
public class StartsWithConditionMatcher implements ConditionMatcher {
    @Override
    public String getConditionType() {
        return "以...开头";
    }

    @Override
    public boolean match(String value, String expression) {
        return value.startsWith(expression);
    }
}