package com.arelore.data.sec.umbrella.server.core.dto.response;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 管理中心账号响应。
 *
 * @author 黄佳豪
 */
@Data
public class AdminUserResponse {
    private Long id;
    private Date createTime;
    private Date modifyTime;
    private String creator;
    private String modifier;
    private String username;
    private String roleCode;
    private List<String> productPermissions;
    private Integer status;
}
