package com.arelore.data.sec.umbrella.server.core.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理中心账号分页查询请求。
 *
 * @author 黄佳豪
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUserQueryRequest extends PageRequest {
    private String username;
    private String roleCode;
    private Integer status;
}
