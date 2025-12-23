package com.arelore.data.sec.umbrella.server.service.impl;

import com.arelore.data.sec.umbrella.server.dto.request.ApiPolicyQueryRequest;
import com.arelore.data.sec.umbrella.server.dto.request.ApiPolicyRequest;
import com.arelore.data.sec.umbrella.server.dto.response.ApiPolicyResponse;
import com.arelore.data.sec.umbrella.server.dto.response.PageResponse;
import com.arelore.data.sec.umbrella.server.entity.ApiPolicy;
import com.arelore.data.sec.umbrella.server.mapper.ApiPolicyMapper;
import com.arelore.data.sec.umbrella.server.service.ApiPolicyService;
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
 * API策略表 服务实现类
 * </p>
 *
 * @author arelore
 * @since 2025-12-24
 */
@Service
public class ApiPolicyServiceImpl extends ServiceImpl<ApiPolicyMapper, ApiPolicy> implements ApiPolicyService {

    @Override
    public PageResponse<ApiPolicyResponse> getPage(ApiPolicyQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<ApiPolicy> queryWrapper = new LambdaQueryWrapper<>();

        // 如果有策略编码，添加查询条件
        if (request.getPolicyCode() != null && !request.getPolicyCode().trim().isEmpty()) {
            queryWrapper.like(ApiPolicy::getPolicyCode, request.getPolicyCode());
        }

        // 如果有策略名称，添加查询条件
        if (request.getPolicyName() != null && !request.getPolicyName().trim().isEmpty()) {
            queryWrapper.like(ApiPolicy::getPolicyName, request.getPolicyName());
        }

        // 如果有敏感等级，添加查询条件
        if (request.getSensitivityLevel() != null) {
            queryWrapper.eq(ApiPolicy::getSensitivityLevel, request.getSensitivityLevel());
        }

        // 创建分页对象
        Page<ApiPolicy> page = new Page<>(request.getCurrent(), request.getSize());

        // 执行分页查询
        IPage<ApiPolicy> pageResult = this.page(page, queryWrapper);

        // 转换为响应对象
        List<ApiPolicyResponse> records = pageResult.getRecords().stream()
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
    public List<ApiPolicyResponse> getAll() {
        List<ApiPolicy> list = this.list();
        return list.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ApiPolicyResponse getById(Long id) {
        ApiPolicy entity = super.getById(id);
        
        if (entity != null) {
            // 转换为响应对象
            ApiPolicyResponse response = new ApiPolicyResponse();
            BeanUtils.copyProperties(entity,response);
            return response;
        }
        
        return null;
    }

    @Override
    public ApiPolicyResponse getByPolicyCode(String policyCode) {
        LambdaQueryWrapper<ApiPolicy> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApiPolicy::getPolicyCode, policyCode);
        ApiPolicy entity = this.getOne(queryWrapper);
        
        if (entity != null) {
            return convertToResponse(entity);
        }
        
        return null;
    }

    @Override
    public Long create(ApiPolicyRequest apiPolicyRequest) {
        ApiPolicy entity = new ApiPolicy();
        BeanUtils.copyProperties(apiPolicyRequest,entity);
        
        boolean success = this.save(entity);
        if (success) {
            return entity.getId();
        }
        
        return null;
    }

    @Override
    public boolean update(Long id, ApiPolicyRequest apiPolicyRequest) {
        ApiPolicy entity = new ApiPolicy();
        BeanUtils.copyProperties(apiPolicyRequest,entity);
        entity.setId(id);

        return this.updateById(entity);
    }

    @Override
    public boolean delete(Long id) {
        return this.removeById(id);
    }

    private ApiPolicyResponse convertToResponse(ApiPolicy entity) {
        ApiPolicyResponse response = new ApiPolicyResponse();
        response.setId(entity.getId());
        BeanUtils.copyProperties(entity,response);
        return response;
    }
}