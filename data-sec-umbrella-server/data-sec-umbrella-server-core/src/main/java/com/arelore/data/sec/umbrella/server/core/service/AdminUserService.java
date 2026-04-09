package com.arelore.data.sec.umbrella.server.core.service;

import com.arelore.data.sec.umbrella.server.core.dto.request.AdminUserQueryRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.AdminUserSaveRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.AdminUserResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 管理中心账号服务。
 *
 * @author 黄佳豪
 */
public interface AdminUserService {

    /**
     * 校验数据库账号登录。
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 命中时返回账号信息，否则返回null
     */
    AdminUserResponse verifyDbUser(String username, String password);

    /**
     * 按用户名查询账号信息。
     *
     * @param username 用户名
     * @return 账号信息，不存在返回null
     */
    AdminUserResponse getByUsername(String username);

    /**
     * 分页查询账号列表。
     *
     * @param request 查询请求
     * @return 分页结果
     */
    IPage<AdminUserResponse> list(AdminUserQueryRequest request);

    /**
     * 新增账号。
     *
     * @param request 保存请求
     * @return 主键ID
     */
    Long create(AdminUserSaveRequest request);

    /**
     * 更新账号。
     *
     * @param request 保存请求
     * @return 是否成功
     */
    boolean update(AdminUserSaveRequest request);

    /**
     * 重置账号密码。
     *
     * @param id 账号ID
     * @param password 新密码
     * @param operator 操作人
     * @return 是否成功
     */
    boolean resetPassword(Long id, String password, String operator);

    /**
     * 删除账号。
     *
     * @param id 账号ID
     * @return 是否成功
     */
    boolean delete(Long id);
}
