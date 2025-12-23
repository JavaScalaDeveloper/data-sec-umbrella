package com.arelore.data.sec.umbrella.server.service;

import com.arelore.data.sec.umbrella.server.dto.request.DatabasePolicyQueryRequest;
import com.arelore.data.sec.umbrella.server.dto.request.DatabasePolicyRequest;
import com.arelore.data.sec.umbrella.server.dto.response.DatabasePolicyResponse;
import com.arelore.data.sec.umbrella.server.dto.response.PageResponse;

import java.util.List;

/**
 * <p>
 * 数据库策略表 服务类
 * </p>
 *
 * @author arelore
 * @since 2025-12-24
 */
public interface DatabasePolicyService {

    /**
     * 分页获取数据库策略
     */
    PageResponse<DatabasePolicyResponse> getPage(DatabasePolicyQueryRequest request);

    /**
     * 获取所有数据库策略
     */
    List<DatabasePolicyResponse> getAll();

    /**
     * 根据ID获取数据库策略
     */
    DatabasePolicyResponse getById(Long id);

    /**
     * 根据策略编码获取数据库策略
     */
    DatabasePolicyResponse getByPolicyCode(String policyCode);

    /**
     * 创建数据库策略
     */
    Long create(DatabasePolicyRequest databasePolicyRequest);

    /**
     * 更新数据库策略
     */
    boolean update(Long id, DatabasePolicyRequest databasePolicyRequest);

    /**
     * 删除数据库策略
     */
    boolean delete(Long id);
}