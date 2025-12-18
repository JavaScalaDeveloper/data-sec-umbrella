package com.arelore.data.sec.umbrella.server.controller;

import com.arelore.data.sec.umbrella.server.entity.DatabasePolicy;
import com.arelore.data.sec.umbrella.server.service.DatabasePolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/database-policy")
public class DatabasePolicyController {

    @Autowired
    private DatabasePolicyService databasePolicyService;

    /**
     * 获取所有数据库策略
     */
    @PostMapping("/list")
    public List<DatabasePolicy> listPolicies(@RequestBody Map<String, Object> params) {
        return databasePolicyService.list();
    }

    /**
     * 根据ID获取数据库策略
     */
    @PostMapping("/get")
    public DatabasePolicy getPolicyById(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        return databasePolicyService.getById(id);
    }

    /**
     * 创建数据库策略
     */
    @PostMapping("/create")
    public Map<String, Object> createPolicy(@RequestBody DatabasePolicy policy) {
        Map<String, Object> result = new HashMap<>();
        boolean success = databasePolicyService.save(policy);
        result.put("success", success);
        result.put("data", policy);
        return result;
    }

    /**
     * 更新数据库策略
     */
    @PostMapping("/update")
    public Map<String, Object> updatePolicy(@RequestBody DatabasePolicy policy) {
        Map<String, Object> result = new HashMap<>();
        boolean success = databasePolicyService.updateById(policy);
        result.put("success", success);
        result.put("data", policy);
        return result;
    }

    /**
     * 删除数据库策略
     */
    @PostMapping("/delete")
    public Map<String, Object> deletePolicy(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        Long id = Long.valueOf(params.get("id").toString());
        boolean success = databasePolicyService.removeById(id);
        result.put("success", success);
        return result;
    }
}