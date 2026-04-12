package com.arelore.data.sec.umbrella.server.core.service.impl.policy;

import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyQueryRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyRuleDetectionRequest;
import com.arelore.data.sec.umbrella.server.core.dto.llm.AiRuleResult;
import com.arelore.data.sec.umbrella.server.core.dto.response.DatabasePolicyResponse;
import com.arelore.data.sec.umbrella.server.core.dto.response.DatabasePolicyRuleDetectionResponse;
import com.arelore.data.sec.umbrella.server.core.dto.response.PageResponse;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.DatabasePolicy;
import com.arelore.data.sec.umbrella.server.core.mapper.DatabasePolicyMapper;
import com.arelore.data.sec.umbrella.server.core.service.DatabasePolicyService;
import com.arelore.data.sec.umbrella.server.core.service.checker.RulesChecker;
import com.arelore.data.sec.umbrella.server.core.service.factory.RulesCheckerFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
        if (StringUtils.hasText(request.getPolicyCode())) {
            queryWrapper.like(DatabasePolicy::getPolicyCode, request.getPolicyCode().trim());
        }
        if (StringUtils.hasText(request.getPolicyName())) {
            queryWrapper.like(DatabasePolicy::getPolicyName, request.getPolicyName().trim());
        }
        if (StringUtils.hasText(request.getCreator())) {
            queryWrapper.like(DatabasePolicy::getCreator, request.getCreator().trim());
        }
        if (StringUtils.hasText(request.getSensitivityLevel())) {
            try {
                queryWrapper.eq(DatabasePolicy::getSensitivityLevel, Integer.parseInt(request.getSensitivityLevel().trim()));
            } catch (NumberFormatException ignore) {
                // ignore invalid sensitivity level
            }
        }
        if (request.getHideExample() != null) {
            queryWrapper.eq(DatabasePolicy::getHideExample, request.getHideExample());
        }
        if (StringUtils.hasText(request.getDatabaseType())) {
            queryWrapper.eq(DatabasePolicy::getDatabaseType, request.getDatabaseType().trim());
        }
        queryWrapper.orderByDesc(DatabasePolicy::getId);
        
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
    public DatabasePolicyRuleDetectionResponse executeRuleDetection(DatabasePolicyRuleDetectionRequest request) {
        DatabasePolicyRuleDetectionResponse ruleResp = executeStructuredRuleDetection(request);
        DatabasePolicyRuleDetectionResponse aiResp = executeAiRuleDetection(request);
        ruleResp.setAiPassed(aiResp.isAiPassed());
        ruleResp.setAiDetail(aiResp.getAiDetail());
        return ruleResp;
    }

    @Override
    public DatabasePolicyRuleDetectionResponse executeStructuredRuleDetection(DatabasePolicyRuleDetectionRequest request) {
        String databaseType = request.getDatabaseType();
        RulesChecker rulesChecker = RulesCheckerFactory.getRulesChecker(databaseType);
        if (rulesChecker == null) {
            DatabasePolicyRuleDetectionResponse response = new DatabasePolicyRuleDetectionResponse();
            response.setRulePassed(false);
            response.setAiPassed(false);
            response.setAiDetail("不支持的数据库类型: " + databaseType);
            return response;
        }

        DatabasePolicyRuleDetectionResponse all = rulesChecker.checkRules(request);
        all.setAiPassed(false);
        all.setAiDetail("AI规则未执行（请调用 POST /api/database-policy/rule-detection-ai-stream）");
        return all;
    }

    @Override
    public DatabasePolicyRuleDetectionResponse executeAiRuleDetection(DatabasePolicyRuleDetectionRequest request) {
        DatabasePolicyRuleDetectionResponse response = new DatabasePolicyRuleDetectionResponse();
        RulesChecker rulesChecker = RulesCheckerFactory.getRulesChecker(request.getDatabaseType());
        if (rulesChecker == null) {
            response.setRulePassed(false);
            response.setAiPassed(false);
            response.setAiDetail("不支持的数据库类型: " + request.getDatabaseType());
            return response;
        }
        AiRuleResult result = rulesChecker.checkAiRules(request.getAiRule(), request.getSamples());
        response.setRulePassed(false);
        response.setAiPassed(result.passed());
        response.setAiDetail(result.detail());
        if (result.passed() && request.getSamples() != null && !request.getSamples().isEmpty()) {
            response.setAiSensitiveSamples(request.getSamples());
        }
        return response;
    }
}