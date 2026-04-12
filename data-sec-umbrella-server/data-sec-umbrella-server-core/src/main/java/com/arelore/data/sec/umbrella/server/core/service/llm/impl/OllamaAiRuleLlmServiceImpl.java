package com.arelore.data.sec.umbrella.server.core.service.llm.impl;

import com.alibaba.fastjson2.JSON;
import com.arelore.data.sec.umbrella.server.core.config.LlmProperties;
import com.arelore.data.sec.umbrella.server.core.dto.llm.AiRuleResult;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyAssetSample;
import com.arelore.data.sec.umbrella.server.core.service.llm.AiRuleLlmService;
import dev.langchain4j.model.ollama.OllamaChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 基于 LangChain4j + Ollama 的 AI 规则评估实现。
 *
 * @author 黄佳豪
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaAiRuleLlmServiceImpl implements AiRuleLlmService {

    private static final Pattern EN_PASS = Pattern.compile("(?i)\\bpass\\b");
    private static final Pattern EN_FAIL = Pattern.compile("(?i)\\bfail\\b");
    /** 匹配「通过」但排除「不通过」「未通过」等包含「通过」子串的否定表述 */
    private static final Pattern CN_PASS = Pattern.compile("(?<!不)(?<!未)通过");

    private final LlmProperties llmProperties;

    @Override
    public AiRuleResult evaluate(String databaseType, String aiRule, List<DatabasePolicyAssetSample> samples) {
        if (!llmProperties.isEnabled()) {
            return new AiRuleResult(false, "LLM未启用，请先配置 llm.ollama.enabled=true", true);
        }
        if (!StringUtils.hasText(aiRule)) {
            return new AiRuleResult(false, "AI规则为空");
        }
        String prompt = buildPrompt(databaseType, aiRule, samples);
        String primary = llmProperties.getModelName();
        String fallback = llmProperties.getFallbackModelName();
        try {
            String answer = doChat(primary, prompt);
            return parseAnswer(answer, primary, false);
        } catch (Exception ex) {
            log.warn("ollama ai evaluate failed with primary model={}, err={}", primary, ex.getMessage());
            if (!StringUtils.hasText(fallback) || fallback.equals(primary)) {
                return new AiRuleResult(false, "LLM调用失败: " + ex.getMessage(), true);
            }
            try {
                String answer = doChat(fallback, prompt);
                return parseAnswer(answer, fallback, true);
            } catch (Exception ex2) {
                log.warn("ollama ai evaluate failed with fallback model={}, err={}", fallback, ex2.getMessage());
                return new AiRuleResult(false, "LLM调用失败(primary+fallback): " + ex2.getMessage(), true);
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
        String text = answer.trim();
        String lower = text.toLowerCase(Locale.ROOT);

        if (EN_FAIL.matcher(lower).find()) {
            return new AiRuleResult(false, prefixModel(answer, modelName, fallbackUsed));
        }
        if (text.contains("不通过") || text.contains("未通过")) {
            return new AiRuleResult(false, prefixModel(answer, modelName, fallbackUsed));
        }

        // 须在 PASS 之前判断：避免「未检出敏感」与「PASS」同段时误判为通过
        if (looksLikeNoSensitiveConclusion(text, lower)) {
            return new AiRuleResult(false,
                    prefixModel(answer, modelName, fallbackUsed) + "（未明确检出敏感内容，结论为未通过）");
        }

        if (EN_PASS.matcher(lower).find() || CN_PASS.matcher(text).find()) {
            return new AiRuleResult(true, prefixModel(answer, modelName, fallbackUsed));
        }
        if (looksLikeSensitiveDetected(text, lower)) {
            return new AiRuleResult(true, prefixModel(answer, modelName, fallbackUsed));
        }

        return new AiRuleResult(false,
                prefixModel("无法从输出中可靠判定为「已检出敏感」，默认未通过：" + text, modelName, fallbackUsed));
    }

    /**
     * 模型明确表示未检出 / 无敏感命中时，不得视为 PASS。
     */
    private static boolean looksLikeNoSensitiveConclusion(String text, String lower) {
        if (text.contains("未检出敏感") || text.contains("未发现敏感") || text.contains("未检测到敏感")) {
            return true;
        }
        if (text.contains("无敏感数据") || text.contains("无敏感资产") || text.contains("没有敏感数据")) {
            return true;
        }
        if (text.contains("不含敏感") || text.contains("未发现符合")) {
            return true;
        }
        if (lower.contains("no sensitive data") || lower.contains("no sensitive information")) {
            return true;
        }
        if (lower.contains("does not contain sensitive") || lower.contains("not sensitive")) {
            return true;
        }
        return false;
    }

    private static boolean looksLikeSensitiveDetected(String text, String lower) {
        if (text.contains("检出敏感") || text.contains("存在敏感") || text.contains("识别为敏感") || text.contains("命中敏感")) {
            return true;
        }
        if (lower.contains("sensitive data detected") || lower.contains("pii detected") || lower.contains("contains pii")) {
            return true;
        }
        return false;
    }

    private static String prefixModel(String body, String modelName, boolean fallbackUsed) {
        if (fallbackUsed) {
            return "[fallback model=" + modelName + "] " + body;
        }
        return body;
    }

    private String buildPrompt(String databaseType, String aiRule, List<DatabasePolicyAssetSample> samples) {
        return "你是数据安全策略助手，负责对「资产样例」做离线规则检测。\n"
                + "数据库类型: " + databaseType + "\n"
                + "AI规则（需在样例中识别出的敏感特征或风险）: " + aiRule + "\n\n"
                + "下方 JSON 数组 `samples` 中，每条对象的字段含义如下（均为字符串或字符串数组，可能为空）：\n"
                + "- databaseName：库名（逻辑/物理库标识）\n"
                + "- databaseDescription：库说明/业务描述\n"
                + "- tableName：表名\n"
                + "- tableDescription：表说明/注释\n"
                + "- columnName：列名\n"
                + "- columnDescription：列说明/注释\n"
                + "- columnValues：该列上采集到的原始单元格值列表（可多条，用于判断列值是否涉敏）\n\n"
                + "样例数据(JSON): " + JSON.toJSONString(samples) + "\n\n"
                + "请严格按下列约定输出：\n"
                + "1) 仅当你**明确**在样例字段（尤其 columnValues 与列元数据）中识别到符合上述 AI 规则描述的敏感内容时，结论为 **PASS**；\n"
                + "2) 若**未检出**敏感、证据不足、或无法判断，结论必须为 **FAIL**（不要把「未检出」写成通过）；\n"
                + "3) 第一行以英文 **PASS** 或 **FAIL** 开头，第二行起用中文简述依据，如果命中了规则，请将命中的内容也一并列出；避免单独使用汉字「通过/不通过」作为唯一结论词，以免解析歧义。\n";
    }
}

