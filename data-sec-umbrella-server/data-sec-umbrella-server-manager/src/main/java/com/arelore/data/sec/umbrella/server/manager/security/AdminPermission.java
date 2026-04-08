package com.arelore.data.sec.umbrella.server.manager.security;

import java.lang.annotation.*;

/**
 * 管理端接口权限注解。
 *
 * @author 黄佳豪
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AdminPermission {
    PermissionAction action() default PermissionAction.READ;
    ProductCode product() default ProductCode.DATABASE;
}
