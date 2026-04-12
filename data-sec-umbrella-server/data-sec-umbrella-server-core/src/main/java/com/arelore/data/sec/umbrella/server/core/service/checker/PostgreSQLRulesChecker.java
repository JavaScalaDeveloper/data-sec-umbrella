package com.arelore.data.sec.umbrella.server.core.service.checker;

import com.arelore.data.sec.umbrella.server.core.dto.llm.AiRuleResult;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyAssetSample;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyClassificationRule;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyRuleDetectionRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.DatabasePolicyRuleDetectionItem;
import com.arelore.data.sec.umbrella.server.core.dto.response.DatabasePolicyRuleDetectionResponse;
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
    public DatabasePolicyRuleDetectionResponse checkRules(DatabasePolicyRuleDetectionRequest request) {
        DatabasePolicyRuleDetectionResponse response = new DatabasePolicyRuleDetectionResponse();

        List<DatabasePolicyRuleDetectionItem> ruleDetails = new ArrayList<>();

        List<DatabasePolicyClassificationRule> classificationRules = request.getClassificationRules();
        if (classificationRules == null) {
            classificationRules = List.of();
        }
        Map<String, Boolean> ruleResults = new HashMap<>();

        for (int i = 0; i < classificationRules.size(); i++) {
            DatabasePolicyClassificationRule rule = classificationRules.get(i);
            DatabasePolicyRuleDetectionItem detail = new DatabasePolicyRuleDetectionItem();
            detail.setRule(i + 1);

            boolean matched = classificationRuleSatisfied(rule, request.getSamples());
            detail.setMatched(matched);
            detail.setDetail(classificationRuleDetail("PostgreSQL", matched, rule, request.getSamples()));

            ruleDetails.add(detail);
            ruleResults.put("rule" + (i + 1), matched);
        }

        response.setRuleDetails(ruleDetails);
        response.setRulePassed(resolveStructuredRulePassed(request.getRuleExpression(), ruleResults));

        response.setAiPassed(false);
        response.setAiDetail("AI规则未执行（请使用 checkAiRules 或管理端 executeAiRuleDetection）");
        return response;
    }

    @Override
    public AiRuleResult checkAiRules(String aiRuleText, List<DatabasePolicyAssetSample> samples) {
        if (aiRuleText == null || aiRuleText.trim().isEmpty()) {
            return new AiRuleResult(false, "AI规则为空");
        }
        return new AiRuleResult(false, "PostgreSQL 尚未接入 LLM AI 规则评估");
    }
}
