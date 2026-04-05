package com.arelore.data.sec.umbrella.server.strategy;

import com.arelore.data.sec.umbrella.server.entity.DataSource;

/**
 * 数据库连接测试策略接口
 */
public interface DatabaseConnectionStrategy {

    /**
     * 测试数据库连接
     * 
     * @param dataSource 数据源配置
     * @throws Exception 连接失败时抛出异常，包含详细错误信息
     */
    void testConnection(DataSource dataSource) throws Exception;

    /**
     * 获取支持的数据库类型
     */
    String getDatabaseType();
}
