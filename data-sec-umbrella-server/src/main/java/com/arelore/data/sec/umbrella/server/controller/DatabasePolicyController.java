package com.arelore.data.sec.umbrella.server.controller;

import com.arelore.data.sec.umbrella.server.entity.DatabasePolicy;
import com.arelore.data.sec.umbrella.server.service.DatabasePolicyService;
import com.arelore.data.sec.umbrella.server.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/database-policy")
public class DatabasePolicyController {

    @Autowired
    private DatabasePolicyService databasePolicyService;

    /**
     * 获取所有数据库策略
     */
    @PostMapping("/list")
    public PageResponseDTO<DatabasePolicy> listPolicies(@RequestBody DatabasePolicyQueryDTO queryDTO) {
        return databasePolicyService.listPoliciesWithPagination(queryDTO);
    }

    /**
     * 根据ID获取数据库策略
     */
    @PostMapping("/get")
    public DatabasePolicy getPolicyById(@RequestBody DatabasePolicyIdDTO idDTO) {
        return databasePolicyService.getById(idDTO.getId());
    }

    /**
     * 创建数据库策略
     */
    @PostMapping("/create")
    public DatabasePolicyResultDTO createPolicy(@RequestBody DatabasePolicy policy) {
        boolean success = databasePolicyService.save(policy);
        DatabasePolicyDTO dto = new DatabasePolicyDTO();
        dto.setId(policy.getId());
        dto.setPolicyCode(policy.getPolicyCode());
        dto.setPolicyName(policy.getPolicyName());
        dto.setPolicyDescription(policy.getDescription());
        dto.setSensitivityLevel(policy.getSensitivityLevel());
        dto.setHideExample(policy.getHideExample());
        dto.setCreateTime(policy.getCreateTime());
        dto.setModifyTime(policy.getModifyTime());
        return DatabasePolicyResultDTO.of(success, dto);
    }

    /**
     * 更新数据库策略
     */
    @PostMapping("/update")
    public DatabasePolicyResultDTO updatePolicy(@RequestBody DatabasePolicy policy) {
        boolean success = databasePolicyService.updateById(policy);
        DatabasePolicyDTO dto = new DatabasePolicyDTO();
        dto.setId(policy.getId());
        dto.setPolicyCode(policy.getPolicyCode());
        dto.setPolicyName(policy.getPolicyName());
        dto.setPolicyDescription(policy.getDescription());
        dto.setSensitivityLevel(policy.getSensitivityLevel());
        dto.setHideExample(policy.getHideExample());
        dto.setCreateTime(policy.getCreateTime());
        dto.setModifyTime(policy.getModifyTime());
        return DatabasePolicyResultDTO.of(success, dto);
    }

    /**
     * 删除数据库策略
     */
    @PostMapping("/delete")
    public OperationResultDTO deletePolicy(@RequestBody DatabasePolicyIdDTO idDTO) {
        boolean success = databasePolicyService.removeById(idDTO.getId());
        return OperationResultDTO.of(success);
    }
}