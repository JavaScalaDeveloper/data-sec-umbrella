package com.arelore.data.sec.umbrella.server.core.service.checker;

import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyTestRulesRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.DatabasePolicyTestRulesResponse;
import com.arelore.data.sec.umbrella.server.core.service.matcher.ConditionMatcher;
import com.arelore.data.sec.umbrella.server.core.util.ConditionObjectValueExtractor;
import com.arelore.data.sec.umbrella.server.core.util.RuleExpressionEvaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PostgreSQL规则检查器
 */
public class PostgreSQLRulesChecker extends AbstractDatabaseRulesChecker {
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }

    @Override
    public DatabasePolicyTestRulesResponse checkRules(DatabasePolicyTestRulesRequest request) {
        DatabasePolicyTestRulesResponse response = new DatabasePolicyTestRulesResponse();
        
        // 初始化规则命中详情列表
        List<DatabasePolicyTestRulesResponse.RuleDetail> ruleDetails = new ArrayList<>();
        
        // 测试分类规则
        boolean allRulesPassed = true;
        List<DatabasePolicyTestRulesRequest.ClassificationRule> classificationRules = request.getClassificationRules();
        Map<String, Boolean> ruleResults = new HashMap<>();
        
        for (int i = 0; i < classificationRules.size(); i++) {
            DatabasePolicyTestRulesRequest.ClassificationRule rule = classificationRules.get(i);
            DatabasePolicyTestRulesResponse.RuleDetail detail = new DatabasePolicyTestRulesResponse.RuleDetail();
            detail.setRule(i + 1);
            
            // PostgreSQL特定的规则测试逻辑
            boolean matched = testSingleRule(rule, request.getTestData());
            detail.setMatched(matched);
            detail.setDetail(matched ? "PostgreSQL规则命中" : "PostgreSQL规则未命中");
            
            ruleDetails.add(detail);
            ruleResults.put("rule" + (i + 1), matched);
            
            if (!matched) {
                allRulesPassed = false;
            }
        }
        
        response.setRulePassed(allRulesPassed);
        response.setRuleDetails(ruleDetails);
        
        // 测试规则表达式
        boolean ruleExpressionPassed = testRuleExpression(request.getRuleExpression(), request.getTestData(), ruleResults);
        response.setRulePassed(response.isRulePassed() && ruleExpressionPassed);
        
        // 测试AI规则
        boolean aiPassed = testAiRule(request.getAiRule(), request.getTestData());
        response.setAiPassed(aiPassed);
        response.setAiDetail(aiPassed ? "PostgreSQL AI规则验证通过" : "PostgreSQL AI规则验证未通过");
        
        return response;
    }
    
    /**
     * 测试单个分类规则
     */
    private boolean testSingleRule(DatabasePolicyTestRulesRequest.ClassificationRule rule, 
                                  List<DatabasePolicyTestRulesRequest.TestData> testData) {
        if (testData == null || testData.isEmpty()) {
            return false;
        }
        
        String conditionObject = rule.getConditionObject();
        String conditionType = rule.getConditionType();
        String expression = rule.getExpression();
        
        for (DatabasePolicyTestRulesRequest.TestData data : testData) {
            String value = getValueByConditionObject(data, conditionObject);
            if (value != null && matchCondition(value, conditionType, expression)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 根据条件对象获取对应的值
     */
    private String getValueByConditionObject(DatabasePolicyTestRulesRequest.TestData data, String conditionObject) {
        return ConditionObjectValueExtractor.getValueByConditionObject(data, conditionObject);
    }
    
    /**
     * 根据条件类型匹配值
     */
    protected boolean matchCondition(String value, String conditionType, String expression) {
        if (value == null || expression == null) {
            return false;
        }
        
        // 根据条件类型获取对应的条件匹配器
        ConditionMatcher conditionMatcher = conditionMatcherMap.get(conditionType);
        
        if (conditionMatcher == null) {
            return false;
        }
        
        // 使用条件匹配器进行匹配
        return conditionMatcher.match(value, expression);
    }
    
    /**
     * 测试规则表达式
     */
    private boolean testRuleExpression(String ruleExpression, List<DatabasePolicyTestRulesRequest.TestData> testData, Map<String, Boolean> ruleResults) {
        return RuleExpressionEvaluator.ruleExpressionCheck(ruleExpression, ruleResults);
    }
    
    /**
     * 测试AI规则
     */
    private boolean testAiRule(String aiRule, List<DatabasePolicyTestRulesRequest.TestData> testData) {
        if (aiRule == null || aiRule.trim().isEmpty()) {
            return false;
        }
        
        // PostgreSQL特定的AI规则测试逻辑
        // 这里只是模拟实现
        return true;
    }
}