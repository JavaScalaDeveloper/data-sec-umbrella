package com.arelore.data.sec.umbrella.server.core.dto.llm;

/**
 * AI 规则（通常为自然语言 + 样例上下文）在某次评估中的结果。
 *
 * @param passed             模型或策略是否判定为「命中 / 通过」
 * @param detail             说明、模型原文或错误信息
 * @param invocationFailed   基础设施类失败（未启用、超时等），与 {@code passed==false} 的「业务未通过」区分
 */
public record AiRuleResult(boolean passed, String detail, boolean invocationFailed) {

    public AiRuleResult(boolean passed, String detail) {
        this(passed, detail, false);
    }
}
