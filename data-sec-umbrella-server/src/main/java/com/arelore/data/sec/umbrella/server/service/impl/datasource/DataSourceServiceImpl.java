package com.arelore.data.sec.umbrella.server.service.impl.datasource;

import com.arelore.data.sec.umbrella.server.constant.RSAKeyConstants;
import com.arelore.data.sec.umbrella.server.dto.request.DataSourceRequest;
import com.arelore.data.sec.umbrella.server.entity.DataSource;
import com.arelore.data.sec.umbrella.server.mapper.DataSourceMapper;
import com.arelore.data.sec.umbrella.server.service.DataSourceService;
import com.arelore.data.sec.umbrella.server.strategy.DatabaseConnectionStrategy;
import com.arelore.data.sec.umbrella.server.strategy.DatabaseConnectionStrategyFactory;
import com.arelore.data.sec.umbrella.server.util.RSACryptoUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
    public IPage<DataSource> list(DataSourceRequest request) {
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
        return this.page(page, queryWrapper);
    }

    @Override
    public DataSource getById(DataSourceRequest request) {
        Long id = request.getId();
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        return this.getById(id);
    }

    @Override
    public Long create(DataSource dataSource) {
        // 密码在前端已经使用RSA公钥加密，这里直接保存加密后的密码
        this.save(dataSource);
        return dataSource.getId();
    }

    @Override
    public boolean update(DataSource dataSource) {
        // 密码在前端已经使用RSA公钥加密，这里直接保存加密后的密码
        return this.updateById(dataSource);
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
    public ConnectionTestResult testConnection(DataSource dataSource) {
        try {
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