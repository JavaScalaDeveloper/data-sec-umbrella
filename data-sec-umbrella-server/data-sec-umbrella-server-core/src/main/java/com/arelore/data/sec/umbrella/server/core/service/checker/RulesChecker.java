package com.arelore.data.sec.umbrella.server.core.service.checker;

import com.arelore.data.sec.umbrella.server.core.dto.llm.AiRuleResult;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyAssetSample;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyRuleDetectionRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.DatabasePolicyRuleDetectionResponse;

import java.util.List;

/**
 * 规则检查器接口
 */
public interface RulesChecker {
    /**
     * 获取支持的数据库类型
     */
    String getDatabaseType();

    /**
     * 检查规则
     */
    DatabasePolicyRuleDetectionResponse checkRules(DatabasePolicyRuleDetectionRequest request);

    /**
     * 对 AI 规则文本与样例做<strong>本数据库引擎</strong>下的评估，与 {@link #checkRules} 的结构化规则完全分离。
     * 实现类可委托 LLM、本地模型或返回「未实现」；样例列表是否允许为空及语义由实现约定。
     *
     * @param aiRuleText 策略侧 AI 规则（多为自然语言）
     * @param samples    参与判定的资产样例（列名、列值等）
     */
    AiRuleResult checkAiRules(String aiRuleText, List<DatabasePolicyAssetSample> samples);
}