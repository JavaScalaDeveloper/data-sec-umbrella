package com.arelore.data.sec.umbrella.server.service.checker;

import com.arelore.data.sec.umbrella.server.dto.request.DatabasePolicyTestRulesRequest;
import com.arelore.data.sec.umbrella.server.dto.response.DatabasePolicyTestRulesResponse;

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
    DatabasePolicyTestRulesResponse checkRules(DatabasePolicyTestRulesRequest request);
}