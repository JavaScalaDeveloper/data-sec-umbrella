package com.arelore.data.sec.umbrella.server.core.util;

import com.alibaba.fastjson2.JSONObject;
import com.googlecode.aviator.AviatorEvaluator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 规则表达式评估工具类
 */
@Slf4j
public class RuleExpressionEvaluator {

    /**
     * 测试规则表达式，支持逻辑运算和位运算
     */
    public static boolean ruleExpressionCheck(String ruleExpression, Map<String, Boolean> ruleResults) {
        if (ruleExpression == null || ruleExpression.trim().isEmpty()) {
            return false;
        }

        try {
            // 将Boolean值转换为数值类型（true→1L, false→0L）
            // 这样可以支持位运算
            Map<String, Object> context = new java.util.HashMap<>();
            for (Map.Entry<String, Boolean> entry : ruleResults.entrySet()) {
                context.put(entry.getKey(), entry.getValue() ? 1L : 0L);
            }
            
            // 将表达式中的数字转换为对应的变量名（如1→rule1）
            String processedExpression = convertNumbersToVariables(ruleExpression);
            
            // 使用Aviator执行规则表达式
            Object result = AviatorEvaluator.execute(processedExpression, context);
            log.info("规则表达式执行结果: ruleExpression: {},processedExpression: {},context: {},result: {}", ruleExpression, processedExpression, JSONObject.toJSONString(context), result);
            // 返回执行结果
            if (result instanceof Boolean) {
                return (Boolean) result;
            } else if (result instanceof Number) {
                return ((Number) result).doubleValue() != 0;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("执行规则表达式失败: {}, 错误信息: {}", ruleExpression, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 将表达式中的数字转换为对应的变量名（如1→rule1）
     */
    private static String convertNumbersToVariables(String expression) {
        return expression.replaceAll("\\b(\\d+)\\b", "rule$1");
    }
}