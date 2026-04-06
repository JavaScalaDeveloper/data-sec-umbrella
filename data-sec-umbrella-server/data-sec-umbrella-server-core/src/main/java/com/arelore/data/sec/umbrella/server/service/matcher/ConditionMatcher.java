package com.arelore.data.sec.umbrella.server.service.matcher;

/**
 * 条件匹配器接口
 */
public interface ConditionMatcher {
    /**
     * 获取支持的条件类型
     */
    String getConditionType();

    /**
     * 匹配条件
     */
    boolean match(String value, String expression);
}