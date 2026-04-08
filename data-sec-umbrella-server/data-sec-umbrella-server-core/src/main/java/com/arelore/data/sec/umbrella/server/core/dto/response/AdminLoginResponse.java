package com.arelore.data.sec.umbrella.server.core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 管理中心登录响应。
 *
 * @author 黄佳豪
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponse {
    private Boolean success;
    private String username;
    private String roleCode;
    private Boolean superAdmin;
    private List<String> productPermissions;
}
