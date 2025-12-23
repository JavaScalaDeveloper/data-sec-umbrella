package com.arelore.data.sec.umbrella.server.service;

import com.arelore.data.sec.umbrella.server.dto.request.ApiPolicyRequest;
import com.arelore.data.sec.umbrella.server.dto.response.ApiPolicyResponse;

import java.util.List;

/**
 * <p>
 * API策略表 服务类
 * </p>
 *
 * @author arelore
 * @since 2025-12-24
 */
public interface ApiPolicyService {

    /**
     * 获取所有API策略
     */
    List<ApiPolicyResponse> getAll();

    /**
     * 根据ID获取API策略
     */
    ApiPolicyResponse getById(Long id);

    /**
     * 根据策略编码获取API策略
     */
    ApiPolicyResponse getByPolicyCode(String policyCode);

    /**
     * 创建API策略
     */
    Long create(ApiPolicyRequest apiPolicyRequest);

    /**
     * 更新API策略
     */
    boolean update(Long id, ApiPolicyRequest apiPolicyRequest);

    /**
     * 删除API策略
     */
    boolean delete(Long id);
}