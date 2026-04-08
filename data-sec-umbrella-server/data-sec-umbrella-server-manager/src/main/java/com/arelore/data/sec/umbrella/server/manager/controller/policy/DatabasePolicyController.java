package com.arelore.data.sec.umbrella.server.manager.controller.policy;

import com.arelore.data.sec.umbrella.server.core.common.Result;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyQueryRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyTestRulesRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.DatabasePolicyResponse;
import com.arelore.data.sec.umbrella.server.core.dto.response.DatabasePolicyTestRulesResponse;
import com.arelore.data.sec.umbrella.server.core.dto.response.PageResponse;
import com.arelore.data.sec.umbrella.server.core.service.DatabasePolicyService;
import com.arelore.data.sec.umbrella.server.manager.service.DatabasePolicyStreamService;
import com.arelore.data.sec.umbrella.server.manager.security.AdminPermission;
import com.arelore.data.sec.umbrella.server.manager.security.PermissionAction;
import com.arelore.data.sec.umbrella.server.manager.security.ProductCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.*;

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
@AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.READ)
public class DatabasePolicyController {

    private final DatabasePolicyService databasePolicyService;
    private final DatabasePolicyStreamService databasePolicyStreamService;

    @Autowired
    public DatabasePolicyController(DatabasePolicyService databasePolicyService,
                                    DatabasePolicyStreamService databasePolicyStreamService) {
        this.databasePolicyService = databasePolicyService;
        this.databasePolicyStreamService = databasePolicyStreamService;
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
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Long> create(@RequestBody DatabasePolicyRequest databasePolicyRequest) {
        Long id = databasePolicyService.create(databasePolicyRequest);
        return Result.success(id);
    }

    @PostMapping("/update")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> update(@RequestBody DatabasePolicyRequest databasePolicyRequest) {
        boolean result = databasePolicyService.update(databasePolicyRequest.getId(), databasePolicyRequest);
        if (result) {
            return Result.success(true);
        }
        return Result.error("更新失败，策略不存在");
    }

    @PostMapping("/delete")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> delete(@RequestBody DatabasePolicyQueryRequest request) {
        boolean result = databasePolicyService.delete(request.getId());
        if (result) {
            return Result.success(true);
        }
        return Result.error("删除失败，策略不存在");
    }

    @PostMapping("/test-rules")
    public Result<DatabasePolicyTestRulesResponse> testRules(@RequestBody DatabasePolicyTestRulesRequest request) {
        DatabasePolicyTestRulesResponse response = databasePolicyService.testRulesOnly(request);
        return Result.success(response);
    }

    @PostMapping(value = "/test-ai-rules-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter testAiRulesStream(@RequestBody DatabasePolicyTestRulesRequest request) {
        return databasePolicyStreamService.testAiRulesStream(request);
    }
}