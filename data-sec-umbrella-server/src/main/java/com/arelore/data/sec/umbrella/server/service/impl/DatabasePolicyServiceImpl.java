package com.arelore.data.sec.umbrella.server.service.impl;

import com.arelore.data.sec.umbrella.server.entity.DatabasePolicy;
import com.arelore.data.sec.umbrella.server.mapper.DatabasePolicyMapper;
import com.arelore.data.sec.umbrella.server.service.DatabasePolicyService;

import com.arelore.data.sec.umbrella.server.dto.DatabasePolicyQueryDTO;
import com.arelore.data.sec.umbrella.server.dto.PageResponseDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

@Service
public class DatabasePolicyServiceImpl extends ServiceImpl<DatabasePolicyMapper, DatabasePolicy> implements DatabasePolicyService {
    
    @Override
    public PageResponseDTO<DatabasePolicy> listPoliciesWithPagination(DatabasePolicyQueryDTO queryDTO) {
        // 创建分页对象
        Page<DatabasePolicy> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        // 创建查询条件
        QueryWrapper<DatabasePolicy> queryWrapper = new QueryWrapper<>();
        
        // 添加查询条件
        if (queryDTO.getPolicyCode() != null && !queryDTO.getPolicyCode().isEmpty()) {
            queryWrapper.like("policy_code", queryDTO.getPolicyCode());
        }
        if (queryDTO.getPolicyName() != null && !queryDTO.getPolicyName().isEmpty()) {
            queryWrapper.like("policy_name", queryDTO.getPolicyName());
        }
        if (queryDTO.getSensitivityLevel() != null) {
            queryWrapper.eq("sensitivity_level", queryDTO.getSensitivityLevel());
        }
        if (queryDTO.getHideExample() != null) {
            queryWrapper.eq("hide_example", queryDTO.getHideExample());
        }
        
        // 按修改时间倒序排列
        queryWrapper.orderByDesc("modify_time");
        
        // 执行分页查询
        Page<DatabasePolicy> resultPage = this.page(page, queryWrapper);
        
        // 构造返回结果
        return PageResponseDTO.of(
            resultPage.getRecords(),
            resultPage.getTotal(),
            Math.toIntExact(resultPage.getCurrent()),
            Math.toIntExact(resultPage.getSize()),
            resultPage.getPages()
        );
    }
}