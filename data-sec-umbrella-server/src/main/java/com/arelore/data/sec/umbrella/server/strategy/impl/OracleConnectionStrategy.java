package com.arelore.data.sec.umbrella.server.strategy.impl;

import com.arelore.data.sec.umbrella.server.entity.DataSource;
import com.arelore.data.sec.umbrella.server.strategy.DatabaseConnectionStrategy;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Oracle数据库连接测试实现类
 */
public class OracleConnectionStrategy implements DatabaseConnectionStrategy {

    private static final String DRIVER_CLASS = "oracle.jdbc.driver.OracleDriver";
    private static final String URL_TEMPLATE = "jdbc:oracle:thin:@%s";

    @Override
    public void testConnection(DataSource dataSource) throws Exception {
        Connection connection = null;
        try {
            // 加载驱动
            Class.forName(DRIVER_CLASS);

            // 构建JDBC URL
            String url = String.format(URL_TEMPLATE, dataSource.getInstance());

            // 建立连接
            connection = DriverManager.getConnection(url, dataSource.getUsername(), dataSource.getPassword());

            // 连接成功，无需返回值
        } catch (ClassNotFoundException e) {
            throw new Exception("Oracle驱动加载失败，请检查驱动配置");
        } catch (Exception e) {
            // 抛出详细的连接错误信息
            throw new Exception("Oracle连接失败: " + e.getMessage());
        } finally {
            // 关闭连接
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    // 忽略关闭连接时的异常
                }
            }
        }
    }

    @Override
    public String getDatabaseType() {
        return "Oracle";
    }
}
