package com.arelore.data.sec.umbrella.server.core.dto.request;

import lombok.Data;

/**
 * 管理中心重置密码请求。
 *
 * @author 黄佳豪
 */
@Data
public class AdminResetPasswordRequest {
    private Long id;
    private String password;
}
