package com.arelore.data.sec.umbrella.server.core.service;

import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyQueryRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyRuleDetectionRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.DatabasePolicyResponse;
import com.arelore.data.sec.umbrella.server.core.dto.response.DatabasePolicyRuleDetectionResponse;
import com.arelore.data.sec.umbrella.server.core.dto.response.PageResponse;

import java.util.List;

/**
 * 数据库策略表 服务类
 */
public interface DatabasePolicyService {

    PageResponse<DatabasePolicyResponse> getPage(DatabasePolicyQueryRequest request);

    List<DatabasePolicyResponse> getAll();

    DatabasePolicyResponse getById(Long id);

    DatabasePolicyResponse getByPolicyCode(String policyCode);

    Long create(DatabasePolicyRequest databasePolicyRequest);

    boolean update(Long id, DatabasePolicyRequest databasePolicyRequest);

    boolean delete(Long id);

    /**
     * 规则检测：分类规则 + 规则表达式 + AI（一次调用完成）。
     */
    DatabasePolicyRuleDetectionResponse executeRuleDetection(DatabasePolicyRuleDetectionRequest request);

    /**
     * 规则检测：仅分类规则与规则表达式（不调用 LLM）。
     */
    DatabasePolicyRuleDetectionResponse executeStructuredRuleDetection(DatabasePolicyRuleDetectionRequest request);

    /**
     * 规则检测：仅 AI 规则（调用 LLM）。
     */
    DatabasePolicyRuleDetectionResponse executeAiRuleDetection(DatabasePolicyRuleDetectionRequest request);
}
