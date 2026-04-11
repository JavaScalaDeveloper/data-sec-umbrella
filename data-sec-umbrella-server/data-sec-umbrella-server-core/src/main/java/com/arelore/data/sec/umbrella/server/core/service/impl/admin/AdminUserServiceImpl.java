package com.arelore.data.sec.umbrella.server.core.service.impl.admin;

import com.arelore.data.sec.umbrella.server.core.dto.request.AdminUserQueryRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.AdminUserSaveRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.AdminUserResponse;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.AdminUser;
import com.arelore.data.sec.umbrella.server.core.mapper.AdminUserMapper;
import com.arelore.data.sec.umbrella.server.core.service.AdminUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 管理中心账号服务实现。
 *
 * @author 黄佳豪
 */
@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {
    private static final String DEFAULT_OPERATOR = "superadmin";

    @Override
    public AdminUserResponse verifyDbUser(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return null;
        }
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminUser::getUsername, username.trim());
        wrapper.eq(AdminUser::getStatus, 1);
        AdminUser user = this.getOne(wrapper);
        if (user == null) {
            return null;
        }
        String hash = sha256(password.trim());
        if (!hash.equals(user.getPasswordHash())) {
            return null;
        }
        return toResponse(user);
    }

    @Override
    public AdminUserResponse getByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        AdminUser user = this.getOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, username.trim()));
        if (user == null) {
            return null;
        }
        return toResponse(user);
    }

    @Override
    public IPage<AdminUserResponse> list(AdminUserQueryRequest request) {
        long current = request.getCurrent() == null ? 1L : request.getCurrent();
        long size = request.getSize() == null ? 10L : request.getSize();
        Page<AdminUser> page = new Page<>(current, size);
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(AdminUser::getUsername, request.getUsername().trim());
        }
        if (StringUtils.hasText(request.getRoleCode())) {
            wrapper.eq(AdminUser::getRoleCode, request.getRoleCode().trim());
        }
        if (request.getStatus() != null) {
            wrapper.eq(AdminUser::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(AdminUser::getCreateTime);
        IPage<AdminUser> entityPage = this.page(page, wrapper);
        Page<AdminUserResponse> responsePage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<AdminUserResponse> records = entityPage.getRecords().stream().map(this::toResponse).collect(Collectors.toList());
        responsePage.setRecords(records);
        return responsePage;
    }

    @Override
    public Long create(AdminUserSaveRequest request) {
        if (!StringUtils.hasText(request.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }
        AdminUser existed = this.getOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, request.getUsername().trim()));
        if (existed != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        AdminUser user = new AdminUser();
        user.setUsername(request.getUsername().trim());
        user.setPasswordHash(sha256(request.getPassword().trim()));
        user.setRoleCode(StringUtils.hasText(request.getRoleCode()) ? request.getRoleCode().trim() : "OPERATOR");
        user.setProductPermissions(joinPermissions(request.getProductPermissions()));
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        user.setCreator(DEFAULT_OPERATOR);
        user.setModifier(DEFAULT_OPERATOR);
        this.save(user);
        return user.getId();
    }

    @Override
    public boolean update(AdminUserSaveRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        AdminUser existed = this.getById(request.getId());
        if (existed == null) {
            return false;
        }
        AdminUser update = new AdminUser();
        update.setId(request.getId());
        if (StringUtils.hasText(request.getUsername())) {
            update.setUsername(request.getUsername().trim());
        }
        if (StringUtils.hasText(request.getPassword())) {
            update.setPasswordHash(sha256(request.getPassword().trim()));
        }
        if (StringUtils.hasText(request.getRoleCode())) {
            update.setRoleCode(request.getRoleCode().trim());
        }
        if (request.getProductPermissions() != null) {
            update.setProductPermissions(joinPermissions(request.getProductPermissions()));
        }
        if (request.getStatus() != null) {
            update.setStatus(request.getStatus());
        }
        update.setModifier(DEFAULT_OPERATOR);
        return this.updateById(update);
    }

    @Override
    public boolean resetPassword(Long id, String password, String operator) {
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("新密码不能为空");
        }
        AdminUser update = new AdminUser();
        update.setId(id);
        update.setPasswordHash(sha256(password.trim()));
        update.setModifier(StringUtils.hasText(operator) ? operator : DEFAULT_OPERATOR);
        return this.updateById(update);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        return this.removeById(id);
    }

    private AdminUserResponse toResponse(AdminUser user) {
        AdminUserResponse response = new AdminUserResponse();
        BeanUtils.copyProperties(user, response);
        response.setProductPermissions(splitPermissions(user.getProductPermissions()));
        return response;
    }

    private String joinPermissions(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "DATABASE";
        }
        return permissions.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.joining(","));
    }

    private List<String> splitPermissions(String permissions) {
        if (!StringUtils.hasText(permissions)) {
            // 历史数据兼容：未配置产品权限时默认给 DATABASE
            return List.of("DATABASE");
        }
        return Arrays.stream(permissions.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("密码摘要失败", ex);
        }
    }
}
