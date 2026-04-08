package com.arelore.data.sec.umbrella.server.manager.controller.policy;

import com.arelore.data.sec.umbrella.server.core.common.Result;
import com.arelore.data.sec.umbrella.server.core.dto.request.ApiPolicyQueryRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.ApiPolicyRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.ApiPolicyResponse;
import com.arelore.data.sec.umbrella.server.core.dto.response.PageResponse;
import com.arelore.data.sec.umbrella.server.core.service.ApiPolicyService;
import com.arelore.data.sec.umbrella.server.manager.security.AdminPermission;
import com.arelore.data.sec.umbrella.server.manager.security.PermissionAction;
import com.arelore.data.sec.umbrella.server.manager.security.ProductCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * API策略表 前端控制器
 * </p>
 *
 * @author arelore
 * @since 2025-12-24
 */
@RestController
@RequestMapping("api/api-policy")
@AdminPermission(product = ProductCode.API, action = PermissionAction.READ)
public class ApiPolicyController {

    private final ApiPolicyService apiPolicyService;

    @Autowired
    public ApiPolicyController(ApiPolicyService apiPolicyService) {
        this.apiPolicyService = apiPolicyService;
    }

    @PostMapping("/list")
    public Result<PageResponse<ApiPolicyResponse>> list(@RequestBody ApiPolicyQueryRequest request) {
        PageResponse<ApiPolicyResponse> pageResponse = apiPolicyService.getPage(request);
        return Result.success(pageResponse);
    }

    @PostMapping("/getById")
    public Result<ApiPolicyResponse> getById(@RequestBody ApiPolicyQueryRequest request) {
        ApiPolicyResponse apiPolicy = apiPolicyService.getById(request.getId());
        if (apiPolicy != null) {
            return Result.success(apiPolicy);
        }
        return Result.error("策略不存在");
    }

    @PostMapping("/getByPolicyCode")
    public Result<ApiPolicyResponse> getByPolicyCode(@RequestBody ApiPolicyQueryRequest request) {
        ApiPolicyResponse apiPolicy = apiPolicyService.getByPolicyCode(request.getPolicyCode());
        if (apiPolicy != null) {
            return Result.success(apiPolicy);
        }
        return Result.error("策略不存在");
    }

    @PostMapping("/create")
    @AdminPermission(product = ProductCode.API, action = PermissionAction.WRITE)
    public Result<Long> create(@RequestBody ApiPolicyRequest apiPolicyRequest) {
        Long id = apiPolicyService.create(apiPolicyRequest);
        return Result.success(id);
    }

    @PostMapping("/update")
    @AdminPermission(product = ProductCode.API, action = PermissionAction.WRITE)
    public Result<Boolean> update(@RequestBody ApiPolicyRequest apiPolicyRequest) {
        boolean result = apiPolicyService.update(apiPolicyRequest.getId(), apiPolicyRequest);
        if (result) {
            return Result.success(true);
        }
        return Result.error("更新失败，策略不存在");
    }

    @PostMapping("/delete")
    @AdminPermission(product = ProductCode.API, action = PermissionAction.WRITE)
    public Result<Boolean> delete(@RequestBody ApiPolicyQueryRequest request) {
        boolean result = apiPolicyService.delete(request.getId());
        if (result) {
            return Result.success(true);
        }
        return Result.error("删除失败，策略不存在");
    }
}