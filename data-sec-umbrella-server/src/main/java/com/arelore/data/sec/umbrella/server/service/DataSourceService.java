package com.arelore.data.sec.umbrella.server.service;

import com.arelore.data.sec.umbrella.server.dto.request.DataSourceRequest;
import com.arelore.data.sec.umbrella.server.dto.response.DataSourceResponse;
import com.arelore.data.sec.umbrella.server.entity.DataSource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 数据源Service接口
 */
public interface DataSourceService extends IService<DataSource> {

    /**
     * 获取RSA公钥
     */
    String getPublicKey();

    /**
     * 分页查询数据源
     */
    IPage<DataSourceResponse> list(DataSourceRequest request);

    /**
     * 根据ID查询数据源
     */
    DataSourceResponse getById(DataSourceRequest request);

    /**
     * 新增数据源
     */
    Long create(DataSourceRequest request);

    /**
     * 更新数据源
     */
    boolean update(DataSourceRequest request);

    /**
     * 删除数据源
     */
    boolean delete(DataSourceRequest request);

    /**
     * 测试数据源连接
     */
    ConnectionTestResult testConnection(DataSourceRequest request);

    /**
     * 连接测试结果
     */
    class ConnectionTestResult {
        private boolean success;
        private String errorMessage;

        public ConnectionTestResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public ConnectionTestResult(boolean success) {
            this(success, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}