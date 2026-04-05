package com.arelore.data.sec.umbrella.server.controller.datasource;

import com.arelore.data.sec.umbrella.server.common.Result;
import com.arelore.data.sec.umbrella.server.entity.DataSource;
import com.arelore.data.sec.umbrella.server.service.DataSourceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 数据源Controller
 */
@RestController
@RequestMapping("api/data-source")
public class DataSourceController {

    private final DataSourceService dataSourceService;

    // RSA公钥（真实密钥，从generate-rsa-keys.sh生成）
    // 请使用 RSA_KEYS.md 中的方法生成新的密钥对
    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA" +
            "u5HPzTZn65ivQJCx7g57pIpmo/qjj+qEUVS5hiGsDaImIpYxPz63sdQ21xJSODhJ" +
            "VCTu2xEcWyD5v1aJQwCibK8a0B0yHzVYuYeRQ3vGgSgsdAhLvQawhmOC6jOeAM" +
            "M4rhXglv3WnD9OCAINcAql10XK46ue0gPBHjnn7MJUe81k8IpwcYgY7XnzWpScm" +
            "M6H0MDTyn/4w6nLciw6LTCmLMBRUTZliQkrAVkG2I8k/5uVw7YSt0ASyIUN0GC" +
            "pBFC0OHIgIWQhJ54AExiKW2Lcg5g3qJA7mYPMmERh/2mSSi7aA4ODO8r8mV7l" +
            "dRVFt5TZq61DalK6IirTwyNCR2ExMwIDAQAB";

    @Autowired
    public DataSourceController(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    /**
     * 获取RSA公钥
     */
    @GetMapping("/public-key")
    public Result<String> getPublicKey() {
        return Result.success(PUBLIC_KEY);
    }

    /**
     * 分页查询数据源
     */
    @GetMapping("/page")
    public Result<IPage<DataSource>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String dataSourceType,
            @RequestParam(required = false) String instance,
            @RequestParam(required = false) String username) {
        Page<DataSource> page = new Page<>(current, size);
        LambdaQueryWrapper<DataSource> queryWrapper = new LambdaQueryWrapper<>();
        if (dataSourceType != null && !dataSourceType.isEmpty()) {
            queryWrapper.eq(DataSource::getDataSourceType, dataSourceType);
        }
        if (instance != null && !instance.isEmpty()) {
            queryWrapper.like(DataSource::getInstance, instance);
        }
        if (username != null && !username.isEmpty()) {
            queryWrapper.eq(DataSource::getUsername, username);
        }
        queryWrapper.orderByDesc(DataSource::getCreateTime);
        IPage<DataSource> result = dataSourceService.page(page, queryWrapper);
        return Result.success(result);
    }

    /**
     * 根据ID查询数据源
     */
    @GetMapping("/{id}")
    public Result<DataSource> getById(@PathVariable Long id) {
        DataSource dataSource = dataSourceService.getById(id);
        return Result.success(dataSource);
    }

    /**
     * 新增数据源
     */
    @PostMapping
    public Result<Long> save(@RequestBody DataSource dataSource) {
        // 密码在前端已经使用RSA公钥加密，这里直接保存加密后的密码
        dataSource.setCreateTime(LocalDateTime.now());
        dataSource.setModifyTime(LocalDateTime.now());
        dataSourceService.save(dataSource);
        return Result.success(dataSource.getId());
    }

    /**
     * 更新数据源
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody DataSource dataSource) {
        // 密码在前端已经使用RSA公钥加密，这里直接保存加密后的密码
        dataSource.setModifyTime(LocalDateTime.now());
        boolean result = dataSourceService.updateById(dataSource);
        return Result.success(result);
    }

    /**
     * 删除数据源
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean result = dataSourceService.removeById(id);
        return Result.success(result);
    }

    /**
     * 测试连接
     */
    @PostMapping("/test-connection")
    public Result<Boolean> testConnection(@RequestBody DataSource dataSource) {
        // 调用service层测试连接（service层会解密密码）
        DataSourceService.ConnectionTestResult result = dataSourceService.testConnection(dataSource);

        if (result.isSuccess()) {
            return Result.success(true);
        } else {
            // 返回错误信息
            return Result.error(result.getErrorMessage());
        }
    }
}