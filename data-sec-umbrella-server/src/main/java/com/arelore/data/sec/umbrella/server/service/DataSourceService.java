package com.arelore.data.sec.umbrella.server.service;

import com.arelore.data.sec.umbrella.server.entity.DataSource;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 数据源Service接口
 */
public interface DataSourceService extends IService<DataSource> {

    /**
     * 测试数据源连接
     */
    ConnectionTestResult testConnection(DataSource dataSource);

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