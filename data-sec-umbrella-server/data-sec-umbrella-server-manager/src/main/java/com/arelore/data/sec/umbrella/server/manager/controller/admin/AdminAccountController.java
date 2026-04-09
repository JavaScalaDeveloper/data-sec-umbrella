package com.arelore.data.sec.umbrella.server.manager.controller.admin;

import com.arelore.data.sec.umbrella.server.core.common.Result;
import com.arelore.data.sec.umbrella.server.core.dto.request.AdminLoginRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.AdminResetPasswordRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.AdminUserQueryRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.AdminUserSaveRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.AdminLoginResponse;
import com.arelore.data.sec.umbrella.server.core.dto.response.AdminUserResponse;
import com.arelore.data.sec.umbrella.server.core.service.AdminUserService;
import com.arelore.data.sec.umbrella.server.manager.security.AdminPermission;
import com.arelore.data.sec.umbrella.server.manager.security.PermissionAction;
import com.arelore.data.sec.umbrella.server.manager.security.ProductCode;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理中心-账号管理接口。
 *
 * @author 黄佳豪
 */
@RestController
@RequestMapping("/api/admin-center/account")
public class AdminAccountController {

    @Value("${admin-center.super-admin.username:root}")
    private String superAdminUsername;

    @Value("${admin-center.super-admin.password:root123}")
    private String superAdminPassword;

    private final AdminUserService adminUserService;

    @Autowired
    public AdminAccountController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            return Result.error("用户名或密码不能为空");
        }
        if (superAdminUsername.equals(request.getUsername().trim()) && superAdminPassword.equals(request.getPassword().trim())) {
            return Result.success(new AdminLoginResponse(true, request.getUsername().trim(), "SUPER_ADMIN", true,
                    java.util.List.of(ProductCode.DATABASE.name(), ProductCode.API.name(), ProductCode.MQ.name(), ProductCode.ADMIN_CENTER.name())));
        }
        AdminUserResponse dbUser = adminUserService.verifyDbUser(request.getUsername(), request.getPassword());
        if (dbUser == null) {
            return Result.error("用户名或密码错误");
        }
        return Result.success(new AdminLoginResponse(true, dbUser.getUsername(), dbUser.getRoleCode(), false, dbUser.getProductPermissions()));
    }

    @PostMapping("/current")
    public Result<AdminLoginResponse> current(HttpServletRequest request) {
        String username = request.getHeader("X-Admin-Username");
        String role = request.getHeader("X-Admin-Role");
        boolean isSuperAdmin = "true".equalsIgnoreCase(request.getHeader("X-Super-Admin"));
        if (!StringUtils.hasText(username)) {
            return Result.error(401, "未登录");
        }
        if (isSuperAdmin || "SUPER_ADMIN".equalsIgnoreCase(role)) {
            return Result.success(new AdminLoginResponse(true, username.trim(), "SUPER_ADMIN", true,
                    java.util.List.of(ProductCode.DATABASE.name(), ProductCode.API.name(), ProductCode.MQ.name(), ProductCode.ADMIN_CENTER.name())));
        }
        AdminUserResponse dbUser = adminUserService.getByUsername(username);
        if (dbUser == null || dbUser.getStatus() == null || dbUser.getStatus() != 1) {
            return Result.error(401, "登录态失效");
        }
        return Result.success(new AdminLoginResponse(true, dbUser.getUsername(), dbUser.getRoleCode(), false, dbUser.getProductPermissions()));
    }

    @PostMapping("/list")
    @AdminPermission(product = ProductCode.ADMIN_CENTER, action = PermissionAction.READ)
    public Result<IPage<AdminUserResponse>> list(@RequestBody AdminUserQueryRequest request) {
        return Result.success(adminUserService.list(request));
    }

    @PostMapping("/create")
    @AdminPermission(product = ProductCode.ADMIN_CENTER, action = PermissionAction.WRITE)
    public Result<Long> create(@RequestBody AdminUserSaveRequest request, HttpServletRequest httpServletRequest) {
        if (!isSuperAdmin(httpServletRequest)) {
            return Result.error(403, "仅超级管理员可新增账号");
        }
        try {
            return Result.success(adminUserService.create(request));
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("/update")
    @AdminPermission(product = ProductCode.ADMIN_CENTER, action = PermissionAction.WRITE)
    public Result<Boolean> update(@RequestBody AdminUserSaveRequest request, HttpServletRequest httpServletRequest) {
        if (!isSuperAdmin(httpServletRequest)) {
            return Result.error(403, "仅超级管理员可修改账号");
        }
        try {
            return Result.success(adminUserService.update(request));
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("/delete")
    @AdminPermission(product = ProductCode.ADMIN_CENTER, action = PermissionAction.WRITE)
    public Result<Boolean> delete(@RequestBody AdminUserSaveRequest request, HttpServletRequest httpServletRequest) {
        if (!isSuperAdmin(httpServletRequest)) {
            return Result.error(403, "仅超级管理员可删除账号");
        }
        try {
            return Result.success(adminUserService.delete(request.getId()));
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("/reset-password")
    @AdminPermission(product = ProductCode.ADMIN_CENTER, action = PermissionAction.WRITE)
    public Result<Boolean> resetPassword(@RequestBody AdminResetPasswordRequest request, HttpServletRequest httpServletRequest) {
        if (!isSuperAdmin(httpServletRequest)) {
            return Result.error(403, "仅超级管理员可重置密码");
        }
        try {
            String operator = httpServletRequest.getHeader("X-Admin-Username");
            return Result.success(adminUserService.resetPassword(request.getId(), request.getPassword(), operator));
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    private boolean isSuperAdmin(HttpServletRequest request) {
        return "true".equalsIgnoreCase(request.getHeader("X-Super-Admin"));
    }
}
