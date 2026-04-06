package com.arelore.data.sec.umbrella.server.service.checker;

import com.arelore.data.sec.umbrella.server.service.matcher.ConditionMatcher;
import com.arelore.data.sec.umbrella.server.service.matcher.ContainsConditionMatcher;
import com.arelore.data.sec.umbrella.server.service.matcher.EndsWithConditionMatcher;
import com.arelore.data.sec.umbrella.server.service.matcher.EqualsConditionMatcher;
import com.arelore.data.sec.umbrella.server.service.matcher.NotContainsConditionMatcher;
import com.arelore.data.sec.umbrella.server.service.matcher.NotEndsWithConditionMatcher;
import com.arelore.data.sec.umbrella.server.service.matcher.NotEqualsConditionMatcher;
import com.arelore.data.sec.umbrella.server.service.matcher.NotStartsWithConditionMatcher;
import com.arelore.data.sec.umbrella.server.service.matcher.RegexConditionMatcher;
import com.arelore.data.sec.umbrella.server.service.matcher.NotRegexConditionMatcher;
import com.arelore.data.sec.umbrella.server.service.matcher.StartsWithConditionMatcher;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库规则检查器抽象类
 */
public abstract class AbstractDatabaseRulesChecker implements RulesChecker {
    
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
}