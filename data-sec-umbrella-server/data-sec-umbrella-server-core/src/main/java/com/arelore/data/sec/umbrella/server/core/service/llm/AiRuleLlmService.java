package com.arelore.data.sec.umbrella.server.core.service.llm;

import com.arelore.data.sec.umbrella.server.core.dto.llm.AiRuleResult;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyAssetSample;

import java.util.List;

/**
 * AI 规则 LLM 评估服务。
 */
public interface AiRuleLlmService {

    /**
     * 执行 AI 规则判断。
     *
     * @param databaseType 数据库类型
     * @param aiRule       AI 规则文本
     * @param samples      参与判断的资产样例
     * @return 判断结果
     */
    AiRuleResult evaluate(String databaseType, String aiRule, List<DatabasePolicyAssetSample> samples);
}
