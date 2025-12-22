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
    public Result<PageResponseDTO<DatabasePolicy>> listPolicies(@RequestBody DatabasePolicyQueryDTO queryDTO) {
        PageResponseDTO<DatabasePolicy> data = databasePolicyService.listPoliciesWithPagination(queryDTO);
        return Result.success(data);
    }

    /**
     * 根据ID获取数据库策略
     */
    @PostMapping("/get")
    public Result<DatabasePolicy> getPolicyById(@RequestBody DatabasePolicyIdDTO idDTO) {
        DatabasePolicy data = databasePolicyService.getById(idDTO.getId());
        return Result.success(data);
    }

    /**
     * 创建数据库策略
     */
    @PostMapping("/create")
    public Result<DatabasePolicy> createPolicy(@RequestBody DatabasePolicy policy) {
        boolean success = databasePolicyService.save(policy);
        if (success) {
            return Result.success(policy);
        } else {
            return Result.error("创建策略失败");
        }
    }

    /**
     * 更新数据库策略
     */
    @PostMapping("/update")
    public Result<DatabasePolicy> updatePolicy(@RequestBody DatabasePolicy policy) {
        boolean success = databasePolicyService.updateById(policy);
        if (success) {
            return Result.success(policy);
        } else {
            return Result.error("更新策略失败");
        }
    }

    /**
     * 删除数据库策略
     */
    @PostMapping("/delete")
    public Result<Boolean> deletePolicy(@RequestBody DatabasePolicyIdDTO idDTO) {
        boolean success = databasePolicyService.removeById(idDTO.getId());
        if (success) {
            return Result.success(true);
        } else {
            return Result.error("删除策略失败");
        }
    }
}