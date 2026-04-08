package com.arelore.data.sec.umbrella.server.core.service.llm.impl;

import com.alibaba.fastjson2.JSON;
import com.arelore.data.sec.umbrella.server.core.config.LlmProperties;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyTestRulesRequest;
import com.arelore.data.sec.umbrella.server.core.service.llm.AiRuleLlmService;
import dev.langchain4j.model.ollama.OllamaChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

/**
 * 基于 LangChain4j + Ollama 的 AI 规则评估实现。
 *
 * @author 黄佳豪
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaAiRuleLlmServiceImpl implements AiRuleLlmService {

    private final LlmProperties llmProperties;

    @Override
    public AiRuleResult evaluate(String databaseType, String aiRule, List<DatabasePolicyTestRulesRequest.TestData> testData) {
        if (!llmProperties.isEnabled()) {
            return new AiRuleResult(false, "LLM未启用，请先配置 llm.ollama.enabled=true");
        }
        if (!StringUtils.hasText(aiRule)) {
            return new AiRuleResult(false, "AI规则为空");
        }
        String prompt = buildPrompt(databaseType, aiRule, testData);
        String primary = llmProperties.getModelName();
        String fallback = llmProperties.getFallbackModelName();
        try {
            String answer = doChat(primary, prompt);
            return parseAnswer(answer, primary, false);
        } catch (Exception ex) {
            log.warn("ollama ai evaluate failed with primary model={}, err={}", primary, ex.getMessage());
            if (!StringUtils.hasText(fallback) || fallback.equals(primary)) {
                return new AiRuleResult(false, "LLM调用失败: " + ex.getMessage());
            }
            try {
                String answer = doChat(fallback, prompt);
                return parseAnswer(answer, fallback, true);
            } catch (Exception ex2) {
                log.warn("ollama ai evaluate failed with fallback model={}, err={}", fallback, ex2.getMessage());
                return new AiRuleResult(false, "LLM调用失败(primary+fallback): " + ex2.getMessage());
            }
        }
    }

    private String doChat(String modelName, String prompt) {
        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl(llmProperties.getBaseUrl())
                .modelName(modelName)
                .timeout(Duration.ofSeconds(llmProperties.getTimeoutSeconds() == null ? 60 : llmProperties.getTimeoutSeconds()))
                .build();
        return model.chat(prompt);
    }

    private AiRuleResult parseAnswer(String answer, String modelName, boolean fallbackUsed) {
        if (!StringUtils.hasText(answer)) {
            return new AiRuleResult(false, "LLM返回为空(model=" + modelName + ")");
        }
        String normalized = answer.trim().toLowerCase();
        boolean passed = normalized.contains("pass") || normalized.contains("true") || normalized.contains("通过");
        if (fallbackUsed) {
            return new AiRuleResult(passed, "[fallback model=" + modelName + "] " + answer);
        }
        return new AiRuleResult(passed, answer);
    }

    private String buildPrompt(String databaseType, String aiRule, List<DatabasePolicyTestRulesRequest.TestData> testData) {
        return "你是数据安全策略判断器。\n"
                + "数据库类型: " + databaseType + "\n"
                + "AI规则: " + aiRule + "\n"
                + "测试数据(JSON): " + JSON.toJSONString(testData) + "\n"
                + "请判断是否命中规则，仅返回简短结论。建议格式：PASS 或 FAIL，并附一行原因。";
    }
}

