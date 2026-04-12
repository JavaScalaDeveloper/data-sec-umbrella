package com.arelore.data.sec.umbrella.server.core.service.checker;

import com.arelore.data.sec.umbrella.server.core.dto.llm.AiRuleResult;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyAssetSample;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyClassificationRule;
import com.arelore.data.sec.umbrella.server.core.service.matcher.ConditionMatcher;
import com.arelore.data.sec.umbrella.server.core.service.matcher.ContainsConditionMatcher;
import com.arelore.data.sec.umbrella.server.core.service.matcher.EndsWithConditionMatcher;
import com.arelore.data.sec.umbrella.server.core.service.matcher.EqualsConditionMatcher;
import com.arelore.data.sec.umbrella.server.core.service.matcher.NotContainsConditionMatcher;
import com.arelore.data.sec.umbrella.server.core.service.matcher.NotEndsWithConditionMatcher;
import com.arelore.data.sec.umbrella.server.core.service.matcher.NotEqualsConditionMatcher;
import com.arelore.data.sec.umbrella.server.core.service.matcher.NotStartsWithConditionMatcher;
import com.arelore.data.sec.umbrella.server.core.service.matcher.RegexConditionMatcher;
import com.arelore.data.sec.umbrella.server.core.service.matcher.NotRegexConditionMatcher;
import com.arelore.data.sec.umbrella.server.core.service.matcher.StartsWithConditionMatcher;
import com.arelore.data.sec.umbrella.server.core.util.ConditionObjectValueExtractor;
import com.arelore.data.sec.umbrella.server.core.util.RuleExpressionEvaluator;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库规则检查器抽象类
 */
public abstract class AbstractDatabaseRulesChecker implements RulesChecker {

    @Override
    public abstract AiRuleResult checkAiRules(String aiRuleText, List<DatabasePolicyAssetSample> samples);

    protected final Map<String, ConditionMatcher> conditionMatcherMap = new HashMap<>();
    
    public AbstractDatabaseRulesChecker() {
        // 初始化条件匹配器映射表
        initConditionMatchers();
    }
    
    /**
     * 初始化条件匹配器
     */
    private void initConditionMatchers() {
        conditionMatcherMap.put("包含", new ContainsConditionMatcher());
        conditionMatcherMap.put("不包含", new NotContainsConditionMatcher());
        conditionMatcherMap.put("等于", new EqualsConditionMatcher());
        conditionMatcherMap.put("不等于", new NotEqualsConditionMatcher());
        conditionMatcherMap.put("以...开头", new StartsWithConditionMatcher());
        conditionMatcherMap.put("不以...开头", new NotStartsWithConditionMatcher());
        conditionMatcherMap.put("以...结尾", new EndsWithConditionMatcher());
        conditionMatcherMap.put("不以...结尾", new NotEndsWithConditionMatcher());
        conditionMatcherMap.put("正则匹配", new RegexConditionMatcher());
        conditionMatcherMap.put("非正则匹配", new NotRegexConditionMatcher());
    }

    /**
     * 统计分类规则在多少条检测样例上命中（每条样例至多计 1 次）。
     */
    protected int countClassificationHits(DatabasePolicyClassificationRule rule,
                                          List<DatabasePolicyAssetSample> testData) {
        if (rule == null || testData == null || testData.isEmpty()) {
            return 0;
        }
        String expression = rule.getExpression();
        if (expression == null) {
            return 0;
        }
        int hits = 0;
        for (DatabasePolicyAssetSample data : testData) {
            String value = ConditionObjectValueExtractor.getValueByConditionObject(data, rule.getConditionObject());
            if (value != null && matchClassificationValue(value, rule.getConditionType(), expression)) {
                hits++;
            }
        }
        return hits;
    }

    protected boolean matchClassificationValue(String value, String conditionType, String expression) {
        if (value == null || expression == null) {
            return false;
        }
        ConditionMatcher conditionMatcher = conditionMatcherMap.get(conditionType);
        return conditionMatcher != null && conditionMatcher.match(value, expression);
    }

    /**
     * 多条检测样例且配置了 {@code ratio}（0–100，表示百分比阈值）时，当命中率 <strong>不低于</strong> {@code ratio} 时通过；
     * 单条样例或未配置 {@code ratio} 时，任一样例命中即通过。
     */
    protected boolean classificationRuleSatisfied(DatabasePolicyClassificationRule rule,
                                                    List<DatabasePolicyAssetSample> testData) {
        if (testData == null || testData.isEmpty()) {
            return false;
        }
        int hits = countClassificationHits(rule, testData);
        if (hits <= 0) {
            return false;
        }
        int n = testData.size();
        Integer ratio = rule.getRatio();
        if (n > 1 && ratio != null) {
            double ratePercent = hits * 100.0 / n;
            return ratePercent >= ratio.doubleValue();
        }
        return true;
    }

    /**
     * 有规则表达式时，整体验证结果仅由表达式决定；无表达式时视为不通过（与 {@link RuleExpressionEvaluator} 对空串行为一致）。
     */
    protected boolean resolveStructuredRulePassed(String ruleExpression, Map<String, Boolean> ruleResults) {
        if (!StringUtils.hasText(ruleExpression)) {
            return false;
        }
        return RuleExpressionEvaluator.ruleExpressionCheck(ruleExpression.trim(), ruleResults);
    }

    protected String classificationRuleDetail(String productLabel,
                                              boolean matched,
                                              DatabasePolicyClassificationRule rule,
                                              List<DatabasePolicyAssetSample> testData) {
        int hits = countClassificationHits(rule, testData);
        int n = testData == null ? 0 : testData.size();
        Integer ratio = rule != null ? rule.getRatio() : null;
        if (n > 1 && ratio != null) {
            double ratePercent = hits * 100.0 / n;
            if (matched) {
                return String.format("%s规则验证通过（命中 %d/%d，%.2f%% ≥ 阈值 %d%%）",
                        productLabel, hits, n, ratePercent, ratio);
            }
            return String.format("%s规则验证未通过（命中 %d/%d，%.2f%%，需命中率 ≥ %d%%）",
                    productLabel, hits, n, ratePercent, ratio);
        }
        return matched ? productLabel + "规则命中" : productLabel + "规则未命中";
    }
}