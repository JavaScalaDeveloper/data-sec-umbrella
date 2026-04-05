package com.arelore.data.sec.umbrella.server.service.impl.policy;

import com.arelore.data.sec.umbrella.server.dto.request.DatabasePolicyQueryRequest;
import com.arelore.data.sec.umbrella.server.dto.request.DatabasePolicyRequest;
import com.arelore.data.sec.umbrella.server.dto.request.DatabasePolicyTestRulesRequest;
import com.arelore.data.sec.umbrella.server.dto.response.DatabasePolicyResponse;
import com.arelore.data.sec.umbrella.server.dto.response.DatabasePolicyTestRulesResponse;
import com.arelore.data.sec.umbrella.server.dto.response.PageResponse;
import com.arelore.data.sec.umbrella.server.entity.DatabasePolicy;
import com.arelore.data.sec.umbrella.server.mapper.DatabasePolicyMapper;
import com.arelore.data.sec.umbrella.server.service.DatabasePolicyService;
import com.arelore.data.sec.umbrella.server.service.checker.RulesChecker;
import com.arelore.data.sec.umbrella.server.service.factory.RulesCheckerFactory;
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
            // 使用BeanUtils.copyProperties复制所有字段，包括databaseType
            DatabasePolicyResponse response = new DatabasePolicyResponse();
            BeanUtils.copyProperties(entity, response);
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
        // 使用BeanUtils.copyProperties复制所有字段，包括databaseType
        BeanUtils.copyProperties(databasePolicyRequest, entity);
        
        boolean success = this.save(entity);
        if (success) {
            return entity.getId();
        }
        
        return null;
    }

    @Override
    public boolean update(Long id, DatabasePolicyRequest databasePolicyRequest) {
        // 先查询现有记录
        DatabasePolicy existingEntity = this.getById(id);
        if (existingEntity == null) {
            return false;
        }
        
        // 使用BeanUtils.copyProperties复制所有字段，包括databaseType
        BeanUtils.copyProperties(databasePolicyRequest, existingEntity);
        
        return this.updateById(existingEntity);
    }

    @Override
    public boolean delete(Long id) {
        return this.removeById(id);
    }

    private DatabasePolicyResponse convertToResponse(DatabasePolicy entity) {
        DatabasePolicyResponse response = new DatabasePolicyResponse();
        // 使用BeanUtils.copyProperties复制所有字段，包括databaseType
        BeanUtils.copyProperties(entity, response);
        return response;
    }

    @Override
    public DatabasePolicyTestRulesResponse testRules(DatabasePolicyTestRulesRequest request) {
        String databaseType = request.getDatabaseType();
        
        // 根据数据库类型获取对应的规则检查器
        RulesChecker rulesChecker = RulesCheckerFactory.getRulesChecker(databaseType);
        
        if (rulesChecker == null) {
            // 如果没有找到对应的规则检查器，使用默认实现
            DatabasePolicyTestRulesResponse response = new DatabasePolicyTestRulesResponse();
            response.setRulePassed(false);
            response.setAiPassed(false);
            response.setAiDetail("不支持的数据库类型: " + databaseType);
            return response;
        }
        
        // 使用对应的规则检查器进行规则检查
        return rulesChecker.checkRules(request);
    }
    
    /**
     * 测试单个分类规则
     */
    private boolean testSingleRule(DatabasePolicyTestRulesRequest.ClassificationRule rule, 
                                  List<DatabasePolicyTestRulesRequest.TestData> testData) {
        if (testData == null || testData.isEmpty()) {
            return false;
        }
        
        String conditionObject = rule.getConditionObject();
        String conditionType = rule.getConditionType();
        String expression = rule.getExpression();
        
        for (DatabasePolicyTestRulesRequest.TestData data : testData) {
            String value = getValueByConditionObject(data, conditionObject);
            if (value != null && matchCondition(value, conditionType, expression)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 根据条件对象获取对应的值
     */
    private String getValueByConditionObject(DatabasePolicyTestRulesRequest.TestData data, String conditionObject) {
        switch (conditionObject) {
            case "库名":
                return data.getDatabaseName();
            case "库描述":
                return data.getDatabaseDescription();
            case "表名":
                return data.getTableName();
            case "表描述":
                return data.getTableDescription();
            case "列名":
                return data.getColumnName();
            case "列描述":
                return data.getColumnDescription();
            case "列值":
                // 如果是列值，返回第一个列值用于测试
                if (data.getColumnValues() != null && !data.getColumnValues().isEmpty()) {
                    return data.getColumnValues().get(0);
                }
                return null;
            default:
                return null;
        }
    }
    
    /**
     * 根据条件类型匹配值
     */
    private boolean matchCondition(String value, String conditionType, String expression) {
        if (value == null || expression == null) {
            return false;
        }
        
        switch (conditionType) {
            case "包含":
                return value.contains(expression);
            case "不包含":
                return !value.contains(expression);
            case "等于":
                return value.equals(expression);
            case "不等于":
                return !value.equals(expression);
            case "以...开头":
                return value.startsWith(expression);
            case "不以...开头":
                return !value.startsWith(expression);
            case "以...结尾":
                return value.endsWith(expression);
            case "不以...结尾":
                return !value.endsWith(expression);
            case "正则匹配":
                try {
                    return value.matches(expression);
                } catch (Exception e) {
                    return false;
                }
            case "非正则匹配":
                try {
                    return !value.matches(expression);
                } catch (Exception e) {
                    return false;
                }
            default:
                return false;
        }
    }
    
    /**
     * 测试规则表达式
     */
    private boolean testRuleExpression(String ruleExpression, List<DatabasePolicyTestRulesRequest.TestData> testData) {
        if (ruleExpression == null || ruleExpression.trim().isEmpty()) {
            return false;
        }
        
        // 简单的规则表达式测试，实际项目中可以使用规则引擎如eviator
        // 这里只是模拟实现
        return true;
    }
    
    /**
     * 测试AI规则
     */
    private boolean testAiRule(String aiRule, List<DatabasePolicyTestRulesRequest.TestData> testData) {
        if (aiRule == null || aiRule.trim().isEmpty()) {
            return false;
        }
        
        // 简单的AI规则测试，实际项目中可以集成AI模型
        // 这里只是模拟实现
        return true;
    }
}