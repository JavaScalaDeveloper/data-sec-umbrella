package com.arelore.data.sec.umbrella.server.core.service.impl.datasource;

import com.arelore.data.sec.umbrella.server.core.constant.RSAKeyConstants;
import com.arelore.data.sec.umbrella.server.core.dto.request.DataSourceRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.DataSourceResponse;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.DataSource;
import com.arelore.data.sec.umbrella.server.core.mapper.DataSourceMapper;
import com.arelore.data.sec.umbrella.server.core.service.DataSourceService;
import com.arelore.data.sec.umbrella.server.core.strategy.DatabaseConnectionStrategy;
import com.arelore.data.sec.umbrella.server.core.strategy.DatabaseConnectionStrategyFactory;
import com.arelore.data.sec.umbrella.server.core.util.RSACryptoUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据源Service实现类
 */
@Service
public class DataSourceServiceImpl extends ServiceImpl<DataSourceMapper, DataSource> implements DataSourceService {

    @Override
    public String getPublicKey() {
        return RSAKeyConstants.PUBLIC_KEY;
    }

    @Override
    public IPage<DataSourceResponse> list(DataSourceRequest request) {
        Integer current = request.getCurrent() != null ? request.getCurrent() : 1;
        Integer size = request.getSize() != null ? request.getSize() : 10;

        Page<DataSource> page = new Page<>(current, size);
        LambdaQueryWrapper<DataSource> queryWrapper = new LambdaQueryWrapper<>();
        if (request.getDataSourceType() != null && !request.getDataSourceType().isEmpty()) {
            queryWrapper.eq(DataSource::getDataSourceType, request.getDataSourceType());
        }
        if (request.getInstance() != null && !request.getInstance().isEmpty()) {
            queryWrapper.like(DataSource::getInstance, request.getInstance());
        }
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            queryWrapper.eq(DataSource::getUsername, request.getUsername());
        }
        queryWrapper.orderByDesc(DataSource::getCreateTime);
        IPage<DataSource> entityPage = this.page(page, queryWrapper);
        Page<DataSourceResponse> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<DataSourceResponse> records = entityPage.getRecords().stream()
                .map(this::toResponseWithoutPassword)
                .collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public DataSourceResponse getById(DataSourceRequest request) {
        Long id = request.getId();
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        DataSource entity = super.getById(id);
        if (entity == null) {
            return null;
        }
        DataSourceResponse response = new DataSourceResponse();
        BeanUtils.copyProperties(entity, response);
        response.setPassword(null);
        return response;
    }

    @Override
    public Long create(DataSourceRequest request) {
        DataSource dataSource = new DataSource();
        BeanUtils.copyProperties(request, dataSource);
        // 密码在前端已经使用RSA公钥加密，这里直接保存加密后的密码
        this.save(dataSource);
        return dataSource.getId();
    }

    @Override
    public boolean update(DataSourceRequest request) {
        DataSource dataSource = new DataSource();
        BeanUtils.copyProperties(request, dataSource);
        Long id = dataSource.getId();
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        DataSource existing = super.getById(id);
        if (existing == null) {
            return false;
        }
        String incomingPassword = dataSource.getPassword();
        if (incomingPassword == null || incomingPassword.isBlank()) {
            // 未传密码或留空：沿用库中已有密文
            dataSource.setPassword(existing.getPassword());
        }
        // 若传了新密码，则为前端 RSA 加密后的密文，直接落库
        return this.updateById(dataSource);
    }

    private DataSourceResponse toResponseWithoutPassword(DataSource entity) {
        DataSourceResponse r = new DataSourceResponse();
        BeanUtils.copyProperties(entity, r);
        r.setPassword(null);
        return r;
    }

    @Override
    public boolean delete(DataSourceRequest request) {
        Long id = request.getId();
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        return this.removeById(id);
    }

    @Override
    public ConnectionTestResult testConnection(DataSourceRequest request) {
        DataSource dataSource = new DataSource();
        BeanUtils.copyProperties(request, dataSource);
        try {
            String pwd = dataSource.getPassword();
            if ((pwd == null || pwd.isBlank()) && dataSource.getId() != null) {
                DataSource existing = super.getById(dataSource.getId());
                if (existing == null || existing.getPassword() == null || existing.getPassword().isBlank()) {
                    return new ConnectionTestResult(false, "未填写密码且库中无已存密码");
                }
                dataSource.setPassword(existing.getPassword());
            }
            if (dataSource.getPassword() == null || dataSource.getPassword().isBlank()) {
                return new ConnectionTestResult(false, "密码不能为空");
            }
            // 使用RSA私钥解密密码
            String decryptedPassword = decryptPassword(dataSource.getPassword());
            dataSource.setPassword(decryptedPassword);

            // 根据数据源类型获取对应的连接策略
            DatabaseConnectionStrategy strategy = DatabaseConnectionStrategyFactory.getStrategy(dataSource.getDataSourceType());

            if (strategy == null) {
                // 不支持的数据库类型
                return new ConnectionTestResult(false, "不支持的数据库类型: " + dataSource.getDataSourceType());
            }

            // 使用策略测试连接，策略会抛出详细的异常
            strategy.testConnection(dataSource);
            return new ConnectionTestResult(true);
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            String errorMessage = e.getMessage();
            return new ConnectionTestResult(false, errorMessage);
        }
    }

    /**
     * 使用RSA私钥解密密码
     */
    private String decryptPassword(String encryptedPassword) {
        try {
            return RSACryptoUtil.decrypt(encryptedPassword, RSAKeyConstants.PRIVATE_KEY);
        } catch (Exception e) {
            throw new RuntimeException("密码解密失败: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        String s = "cNlHAlRaHhP2x4sUaQAX6/bJYFMeGjx7B9EN67IltZeO3AGGgzJYnwzkPzpmZ0JUEhRFh1SeimUCoPxMfO1KYKRvbS5Q1uJAs8QY71zxE/FtQA6lJFPKWx2jlZdu1yMbQZjzph0nuR2TJ8MdFXLVPy6ojVuarM3SR9qTczd4HMYWRSVjo6MV81CsSjo0Sk0mcsGpUOul8b0sWdNVXx97cMpObiaKvocKFZKp5P13OmCt4zrg5HN2uuwUguogtxNrj4s3E4nfhqeveSiuuOxtts+8EQM53fgukwt9yrAHoN7J3yXckxYYsDRYy2JKu6efumhO1kUBA0qFTaI0rWrsEw==";
        System.out.println(s.length());
    }
}