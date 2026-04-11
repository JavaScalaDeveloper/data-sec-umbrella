package com.arelore.data.sec.umbrella.server.core.entity.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 管理中心账号实体。
 *
 * @author 黄佳豪
 */
@Data
@TableName("admin_user")
public class AdminUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Date createTime;

    private Date modifyTime;

    /**
     * 创建人。
     */
    private String creator;

    /**
     * 修改人。
     */
    private String modifier;

    private String username;

    /**
     * 密码摘要（SHA-256十六进制）。
     */
    private String passwordHash;

    /**
     * 角色编码（如 ADMIN/OPERATOR）。
     */
    private String roleCode;

    /**
     * 产品权限列表（逗号分隔）：DATABASE,API,MQ。
     */
    private String productPermissions;

    /**
     * 状态：1启用，0禁用。
     */
    private Integer status;
}
