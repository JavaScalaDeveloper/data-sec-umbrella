package com.arelore.data.sec.umbrella.server.core.strategy.impl;

import com.arelore.data.sec.umbrella.server.core.entity.mysql.DataSource;
import com.arelore.data.sec.umbrella.server.core.strategy.DatabaseConnectionStrategy;
import com.arelore.data.sec.umbrella.server.core.util.ClickHouseJdbcUrl;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * ClickHouse 数据源连通性测试（JDBC HTTP 接口，实例格式 host:port）。
 */
public class ClickHouseConnectionStrategy implements DatabaseConnectionStrategy {

    private static final String DRIVER_CLASS = "com.clickhouse.jdbc.ClickHouseDriver";

    @Override
    public void testConnection(DataSource dataSource) throws Exception {
        Connection connection = null;
        try {
            Class.forName(DRIVER_CLASS);
            String url = ClickHouseJdbcUrl.build(dataSource.getInstance(), "default");
            connection = DriverManager.getConnection(url, dataSource.getUsername(), dataSource.getPassword());
        } catch (ClassNotFoundException e) {
            throw new Exception("ClickHouse JDBC 驱动未找到，请检查依赖");
        } catch (Exception e) {
            throw new Exception("ClickHouse 连接失败: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignored) {
                    // ignore
                }
            }
        }
    }

    @Override
    public String getDatabaseType() {
        return "Clickhouse";
    }
}
