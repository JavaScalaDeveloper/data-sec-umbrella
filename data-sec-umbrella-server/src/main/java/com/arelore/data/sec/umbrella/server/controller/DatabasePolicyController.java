package com.arelore.data.sec.umbrella.server.controller;

import com.arelore.data.sec.umbrella.server.common.Result;
import com.arelore.data.sec.umbrella.server.dto.request.DatabasePolicyQueryRequest;
import com.arelore.data.sec.umbrella.server.dto.request.DatabasePolicyRequest;
import com.arelore.data.sec.umbrella.server.dto.response.DatabasePolicyResponse;
import com.arelore.data.sec.umbrella.server.dto.response.PageResponse;
import com.arelore.data.sec.umbrella.server.service.DatabasePolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 数据库策略表 前端控制器
 * </p>
 *
 * @author arelore
 * @since 2025-12-24
 */
@RestController
@RequestMapping("api/database-policy")
public class DatabasePolicyController {

    private final DatabasePolicyService databasePolicyService;

    @Autowired
    public DatabasePolicyController(DatabasePolicyService databasePolicyService) {
        this.databasePolicyService = databasePolicyService;
    }

    @PostMapping("/list")
    public Result<PageResponse<DatabasePolicyResponse>> list(@RequestBody DatabasePolicyQueryRequest request) {
        PageResponse<DatabasePolicyResponse> pageResponse = databasePolicyService.getPage(request);
        return Result.success(pageResponse);
    }

    @PostMapping("/getById")
    public Result<DatabasePolicyResponse> getById(@RequestBody DatabasePolicyQueryRequest request) {
        DatabasePolicyResponse databasePolicy = databasePolicyService.getById(request.getId());
        if (databasePolicy != null) {
            return Result.success(databasePolicy);
        }
        return Result.error("策略不存在");
    }

    @PostMapping("/getByPolicyCode")
    public Result<DatabasePolicyResponse> getByPolicyCode(@RequestBody DatabasePolicyQueryRequest request) {
        DatabasePolicyResponse databasePolicy = databasePolicyService.getByPolicyCode(request.getPolicyCode());
        if (databasePolicy != null) {
            return Result.success(databasePolicy);
        }
        return Result.error("策略不存在");
    }

    @PostMapping("/create")
    public Result<Long> create(@RequestBody DatabasePolicyRequest databasePolicyRequest) {
        Long id = databasePolicyService.create(databasePolicyRequest);
        return Result.success(id);
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody DatabasePolicyRequest databasePolicyRequest) {
        boolean result = databasePolicyService.update(databasePolicyRequest.getId(), databasePolicyRequest);
        if (result) {
            return Result.success(true);
        }
        return Result.error("更新失败，策略不存在");
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody DatabasePolicyQueryRequest request) {
        boolean result = databasePolicyService.delete(request.getId());
        if (result) {
            return Result.success(true);
        }
        return Result.error("删除失败，策略不存在");
    }
}