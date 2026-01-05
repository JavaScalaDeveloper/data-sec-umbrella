package com.arelore.data.sec.umbrella.server.service.impl;

import com.arelore.data.sec.umbrella.server.dto.request.DatabasePolicyQueryRequest;
import com.arelore.data.sec.umbrella.server.dto.request.DatabasePolicyRequest;
import com.arelore.data.sec.umbrella.server.dto.response.DatabasePolicyResponse;
import com.arelore.data.sec.umbrella.server.dto.response.PageResponse;
import com.arelore.data.sec.umbrella.server.entity.DatabasePolicy;
import com.arelore.data.sec.umbrella.server.mapper.DatabasePolicyMapper;
import com.arelore.data.sec.umbrella.server.service.DatabasePolicyService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 数据库策略表 服务实现类
 * </p>
 *
 * @author arelore
 * @since 2025-12-24
 */
@Service
public class DatabasePolicyServiceImpl extends ServiceImpl<DatabasePolicyMapper, DatabasePolicy> implements DatabasePolicyService {

    @Override
    public PageResponse<DatabasePolicyResponse> getPage(DatabasePolicyQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<DatabasePolicy> queryWrapper = new LambdaQueryWrapper<>();
        
        // 如果有策略编码，添加查询条件
        if (request.getPolicyCode() != null && !request.getPolicyCode().trim().isEmpty()) {
            queryWrapper.like(DatabasePolicy::getPolicyCode, request.getPolicyCode());
        }
        
        // 创建分页对象
        Page<DatabasePolicy> page = new Page<>(request.getCurrent(), request.getSize());
        
        // 执行分页查询
        IPage<DatabasePolicy> pageResult = this.page(page, queryWrapper);
        
        // 转换为响应对象
        List<DatabasePolicyResponse> records = pageResult.getRecords().stream()
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
    public List<DatabasePolicyResponse> getAll() {
        List<DatabasePolicy> list = this.list();
        return list.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public DatabasePolicyResponse getById(Long id) {
        DatabasePolicy entity = super.getById(id);
        
        if (entity != null) {
            // 转换为响应对象
            DatabasePolicyResponse response = new DatabasePolicyResponse();
            response.setId(entity.getId());
            response.setPolicyCode(entity.getPolicyCode());
            response.setPolicyName(entity.getPolicyName());
            response.setDescription(entity.getDescription());
            response.setSensitivityLevel(entity.getSensitivityLevel());
            response.setHideExample(entity.getHideExample());
            response.setClassificationRules(entity.getClassificationRules());
            response.setRuleExpression(entity.getRuleExpression());
            response.setAiRule(entity.getAiRule());

            response.setCreateTime(entity.getCreateTime());
            response.setModifyTime(entity.getModifyTime());
            return response;
        }
        
        return null;
    }

    @Override
    public DatabasePolicyResponse getByPolicyCode(String policyCode) {
        LambdaQueryWrapper<DatabasePolicy> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DatabasePolicy::getPolicyCode, policyCode);
        DatabasePolicy entity = this.getOne(queryWrapper);
        
        if (entity != null) {
            return convertToResponse(entity);
        }
        
        return null;
    }

    @Override
    public Long create(DatabasePolicyRequest databasePolicyRequest) {
        DatabasePolicy entity = new DatabasePolicy();
        entity.setPolicyCode(databasePolicyRequest.getPolicyCode());
        entity.setPolicyName(databasePolicyRequest.getPolicyName());
        entity.setDescription(databasePolicyRequest.getDescription());
        entity.setSensitivityLevel(databasePolicyRequest.getSensitivityLevel());
        entity.setHideExample(databasePolicyRequest.getHideExample());
        entity.setClassificationRules(databasePolicyRequest.getClassificationRules());
        entity.setRuleExpression(databasePolicyRequest.getRuleExpression());
        entity.setAiRule(databasePolicyRequest.getAiRule());

        entity.setCreateTime(LocalDateTime.now());
        entity.setModifyTime(LocalDateTime.now());
        
        boolean success = this.save(entity);
        if (success) {
            return entity.getId();
        }
        
        return null;
    }

    @Override
    public boolean update(Long id, DatabasePolicyRequest databasePolicyRequest) {
        DatabasePolicy entity = new DatabasePolicy();
        entity.setId(id);
        entity.setPolicyCode(databasePolicyRequest.getPolicyCode());
        entity.setPolicyName(databasePolicyRequest.getPolicyName());
        entity.setDescription(databasePolicyRequest.getDescription());
        entity.setSensitivityLevel(databasePolicyRequest.getSensitivityLevel());
        entity.setHideExample(databasePolicyRequest.getHideExample());
        entity.setClassificationRules(databasePolicyRequest.getClassificationRules());
        entity.setRuleExpression(databasePolicyRequest.getRuleExpression());
        entity.setAiRule(databasePolicyRequest.getAiRule());

        entity.setModifyTime(LocalDateTime.now());
        
        return this.updateById(entity);
    }

    @Override
    public boolean delete(Long id) {
        return this.removeById(id);
    }

    private DatabasePolicyResponse convertToResponse(DatabasePolicy entity) {
        DatabasePolicyResponse response = new DatabasePolicyResponse();
        response.setId(entity.getId());
        response.setPolicyCode(entity.getPolicyCode());
        response.setPolicyName(entity.getPolicyName());
        response.setDescription(entity.getDescription());
        response.setSensitivityLevel(entity.getSensitivityLevel());
        response.setHideExample(entity.getHideExample());
        response.setClassificationRules(entity.getClassificationRules());
        response.setRuleExpression(entity.getRuleExpression());
        response.setAiRule(entity.getAiRule());

        response.setCreateTime(entity.getCreateTime());
        response.setModifyTime(entity.getModifyTime());
        return response;
    }
}