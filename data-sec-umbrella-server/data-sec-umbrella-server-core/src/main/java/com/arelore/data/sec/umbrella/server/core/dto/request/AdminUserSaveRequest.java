package com.arelore.data.sec.umbrella.server.core.dto.request;

import lombok.Data;

/**
 * 管理中心账号保存请求。
 *
 * @author 黄佳豪
 */
@Data
public class AdminUserSaveRequest {
    private Long id;
    private String username;
    private String password;
    private String roleCode;
    private Integer status;
    private java.util.List<String> productPermissions;
}
