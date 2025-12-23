package com.arelore.data.sec.umbrella.server.service.impl;

import com.arelore.data.sec.umbrella.server.dto.request.MessagePolicyQueryRequest;
import com.arelore.data.sec.umbrella.server.dto.request.MessagePolicyRequest;
import com.arelore.data.sec.umbrella.server.dto.response.MessagePolicyResponse;
import com.arelore.data.sec.umbrella.server.dto.response.PageResponse;
import com.arelore.data.sec.umbrella.server.entity.MessagePolicy;
import com.arelore.data.sec.umbrella.server.mapper.MessagePolicyMapper;
import com.arelore.data.sec.umbrella.server.service.MessagePolicyService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 消息策略表 服务实现类
 * </p>
 *
 * @author arelore
 * @since 2025-12-24
 */
@Service
public class MessagePolicyServiceImpl extends ServiceImpl<MessagePolicyMapper, MessagePolicy> implements MessagePolicyService {

    @Override
    public PageResponse<MessagePolicyResponse> getPage(MessagePolicyQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<MessagePolicy> queryWrapper = new LambdaQueryWrapper<>();

        // 如果有策略编码，添加查询条件
        if (request.getPolicyCode() != null && !request.getPolicyCode().trim().isEmpty()) {
            queryWrapper.like(MessagePolicy::getPolicyCode, request.getPolicyCode());
        }

        // 如果有策略名称，添加查询条件
        if (request.getPolicyName() != null && !request.getPolicyName().trim().isEmpty()) {
            queryWrapper.like(MessagePolicy::getPolicyName, request.getPolicyName());
        }

        // 创建分页对象
        Page<MessagePolicy> page = new Page<>(request.getCurrent(), request.getSize());

        // 执行分页查询
        IPage<MessagePolicy> pageResult = this.page(page, queryWrapper);

        // 转换为响应对象
        List<MessagePolicyResponse> records = pageResult.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        // 构建分页响应
        return new PageResponse<>(
                records,
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize()
        );
    }

    @Override
    public List<MessagePolicyResponse> getAll() {
        List<MessagePolicy> list = this.list();
        return list.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public MessagePolicyResponse getById(Long id) {
        MessagePolicy entity = super.getById(id);

        if (entity != null) {
            // 转换为响应对象
            MessagePolicyResponse response = new MessagePolicyResponse();
            response.setId(entity.getId());
            BeanUtils.copyProperties(entity, response);
            return response;
        }

        return null;
    }

    @Override
    public MessagePolicyResponse getByPolicyCode(String policyCode) {
        LambdaQueryWrapper<MessagePolicy> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MessagePolicy::getPolicyCode, policyCode);
        MessagePolicy entity = this.getOne(queryWrapper);

        if (entity != null) {
            return convertToResponse(entity);
        }

        return null;
    }

    @Override
    public Long create(MessagePolicyRequest messagePolicyRequest) {
        MessagePolicy entity = new MessagePolicy();
        BeanUtils.copyProperties(messagePolicyRequest, entity);
        boolean success = this.save(entity);
        if (success) {
            return entity.getId();
        }

        return null;
    }

    @Override
    public boolean update(Long id, MessagePolicyRequest messagePolicyRequest) {
        MessagePolicy entity = new MessagePolicy();
        BeanUtils.copyProperties(messagePolicyRequest, entity);
        entity.setId(id);

        return this.updateById(entity);
    }

    @Override
    public boolean delete(Long id) {
        return this.removeById(id);
    }

    private MessagePolicyResponse convertToResponse(MessagePolicy entity) {
        MessagePolicyResponse response = new MessagePolicyResponse();
        BeanUtils.copyProperties(entity, response);

        return response;
    }
}