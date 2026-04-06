package com.arelore.data.sec.umbrella.server.core.strategy.impl;

import com.arelore.data.sec.umbrella.server.core.entity.DataSource;
import com.arelore.data.sec.umbrella.server.core.strategy.DatabaseConnectionStrategy;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * MySQL数据库连接测试实现类
 */
public class MySQLConnectionStrategy implements DatabaseConnectionStrategy {

    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final String URL_TEMPLATE = "jdbc:mysql://%s?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";

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
            throw new Exception("MySQL驱动加载失败，请检查驱动配置");
        } catch (Exception e) {
            // 抛出详细的连接错误信息
            throw new Exception("MySQL连接失败: " + e.getMessage());
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
        return "MySQL";
    }
}
