package com.arelore.data.sec.umbrella.server.core.service.llm;

import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyTestRulesRequest;

import java.util.List;

/**
 * AI 规则 LLM 评估服务。
 *
 * @author 黄佳豪
 */
public interface AiRuleLlmService {

    /**
     * 执行 AI 规则判断。
     *
     * @param databaseType 数据库类型
     * @param aiRule       AI 规则文本
     * @param testData     测试数据
     * @return 判断结果
     */
    AiRuleResult evaluate(String databaseType, String aiRule, List<DatabasePolicyTestRulesRequest.TestData> testData);

    /**
     * AI 规则评估结果。
     */
    record AiRuleResult(boolean passed, String detail) {
    }
}

