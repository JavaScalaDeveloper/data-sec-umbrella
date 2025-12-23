package com.arelore.data.sec.umbrella.server.service;

import com.arelore.data.sec.umbrella.server.dto.request.MessagePolicyRequest;
import com.arelore.data.sec.umbrella.server.dto.response.MessagePolicyResponse;

import java.util.List;

/**
 * <p>
 * 消息策略表 服务类
 * </p>
 *
 * @author arelore
 * @since 2025-12-24
 */
public interface MessagePolicyService {

    /**
     * 获取所有消息策略
     */
    List<MessagePolicyResponse> getAll();

    /**
     * 根据ID获取消息策略
     */
    MessagePolicyResponse getById(Long id);

    /**
     * 根据策略编码获取消息策略
     */
    MessagePolicyResponse getByPolicyCode(String policyCode);

    /**
     * 创建消息策略
     */
    Long create(MessagePolicyRequest messagePolicyRequest);

    /**
     * 更新消息策略
     */
    boolean update(Long id, MessagePolicyRequest messagePolicyRequest);

    /**
     * 删除消息策略
     */
    boolean delete(Long id);
}