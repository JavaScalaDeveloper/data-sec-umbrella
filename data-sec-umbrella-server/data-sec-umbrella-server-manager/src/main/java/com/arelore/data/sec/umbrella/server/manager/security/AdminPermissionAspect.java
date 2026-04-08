package com.arelore.data.sec.umbrella.server.manager.security;

import com.arelore.data.sec.umbrella.server.core.common.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 管理端权限切面。
 *
 * @author 黄佳豪
 */
@Aspect
@Component
public class AdminPermissionAspect {

    private final HttpServletRequest request;

    public AdminPermissionAspect(HttpServletRequest request) {
        this.request = request;
    }

    @Around("@within(com.arelore.data.sec.umbrella.server.manager.security.AdminPermission) || @annotation(com.arelore.data.sec.umbrella.server.manager.security.AdminPermission)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        AdminPermission methodAnno = method.getAnnotation(AdminPermission.class);
        AdminPermission classAnno = joinPoint.getTarget().getClass().getAnnotation(AdminPermission.class);
        AdminPermission permission = methodAnno != null ? methodAnno : classAnno;
        if (permission == null) {
            return joinPoint.proceed();
        }

        String role = valueOfHeader("X-Admin-Role");
        String superAdmin = valueOfHeader("X-Super-Admin");
        String permissionHeader = valueOfHeader("X-Product-Permissions");

        // 兼容旧链路：未登录态请求先放行
        if (!StringUtils.hasText(role) && !StringUtils.hasText(superAdmin)) {
            return joinPoint.proceed();
        }
        if ("true".equalsIgnoreCase(superAdmin) || "SUPER_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
            return joinPoint.proceed();
        }

        if ("OPERATOR".equalsIgnoreCase(role) && permission.action() == PermissionAction.WRITE) {
            return Result.error(403, "当前角色仅支持查询接口");
        }

        Set<String> productPermissions = new HashSet<>();
        if (StringUtils.hasText(permissionHeader)) {
            productPermissions.addAll(Arrays.asList(permissionHeader.split(",")));
        }
        if (!productPermissions.contains(permission.product().name())) {
            return Result.error(403, "当前账号无该产品权限");
        }
        return joinPoint.proceed();
    }

    private String valueOfHeader(String key) {
        String value = request.getHeader(key);
        return value == null ? "" : value.trim();
    }
}
