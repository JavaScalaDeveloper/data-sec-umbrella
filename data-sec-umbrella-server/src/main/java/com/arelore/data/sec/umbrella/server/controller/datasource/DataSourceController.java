package com.arelore.data.sec.umbrella.server.controller.datasource;

import com.arelore.data.sec.umbrella.server.common.Result;
import com.arelore.data.sec.umbrella.server.dto.request.DataSourceRequest;
import com.arelore.data.sec.umbrella.server.dto.response.DataSourceResponse;
import com.arelore.data.sec.umbrella.server.entity.DataSource;
import com.arelore.data.sec.umbrella.server.service.DataSourceService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据源Controller
 *
 * ⚠️ 接口规范说明：
 * 1. 禁止使用 RESTful 风格，所有接口统一使用 POST 方法
 * 2. 禁止将参数写在 URL 路径中，所有参数必须放在请求体中
 * 3. 所有接口返回统一格式的 Result 对象
 */
@RestController
@RequestMapping("api/data-source")
public class DataSourceController {

    private final DataSourceService dataSourceService;

    @Autowired
    public DataSourceController(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    /**
     * 获取RSA公钥
     */
    @PostMapping("/get-public-key")
    public Result<String> getPublicKey() {
        return Result.success(dataSourceService.getPublicKey());
    }

    /**
     * 分页查询数据源
     */
    @PostMapping("/list")
    public Result<IPage<DataSource>> list(@RequestBody DataSourceRequest request) {
        long ts1 = System.currentTimeMillis();
        IPage<DataSource> result = dataSourceService.list(request);
        long ts2 = System.currentTimeMillis();
        System.out.println("list cost: " + (ts2 - ts1) + "ms");
        return Result.success(result);
    }

    /**
     * 根据ID查询数据源
     */
    @PostMapping("/get-by-id")
    public Result<DataSourceResponse> getById(@RequestBody DataSourceRequest request) {
        DataSource dataSource = dataSourceService.getById(request);
        DataSourceResponse response = new DataSourceResponse();
        org.springframework.beans.BeanUtils.copyProperties(dataSource, response);
        return Result.success(response);
    }

    /**
     * 新增数据源
     */
    @PostMapping("/create")
    public Result<Long> create(@RequestBody DataSource dataSource) {
        Long id = dataSourceService.create(dataSource);
        return Result.success(id);
    }

    /**
     * 更新数据源
     */
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody DataSource dataSource) {
        boolean result = dataSourceService.update(dataSource);
        return Result.success(result);
    }

    /**
     * 删除数据源
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody DataSourceRequest request) {
        boolean result = dataSourceService.delete(request);
        return Result.success(result);
    }

    /**
     * 测试连接
     */
    @PostMapping("/test-connection")
    public Result<Boolean> testConnection(@RequestBody DataSource dataSource) {
        DataSourceService.ConnectionTestResult result = dataSourceService.testConnection(dataSource);
        if (result.isSuccess()) {
            return Result.success(true);
        } else {
            return Result.error(result.getErrorMessage());
        }
    }
}