package com.arelore.data.sec.umbrella.server.util;

import com.googlecode.aviator.AviatorEvaluator;

import java.util.Map;

/**
 * 规则表达式评估工具类
 */
public class RuleExpressionEvaluator {
    
    /**
     * 测试规则表达式
     */
    public static boolean testRuleExpression(String ruleExpression, Map<String, Boolean> ruleResults) {
        if (ruleExpression == null || ruleExpression.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 使用分类规则的结果作为Aviator上下文
            Map<String, Object> context = Map.copyOf(ruleResults);
            
            // 使用Aviator执行规则表达式
            Object result = AviatorEvaluator.execute(ruleExpression, context);
            
            // 返回执行结果
            return result instanceof Boolean && (Boolean) result;
        } catch (Exception e) {
            return false;
        }
    }
}