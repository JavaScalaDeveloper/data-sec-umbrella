package com.arelore.data.sec.umbrella.server.core.strategy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据库扫描策略接口
 */
public interface DatabaseScanStrategy {

    /**
     * 获取数据库查询语句
     */
    String getDatabaseQuery();

    /**
     * 获取表查询语句
     *
     * @param databaseName 数据库名
     */
    String getTableQuery(String databaseName);

    /**
     * 从结果集中获取表名和表注释
     *
     * @param resultSet 结果集
     * @return 表名和表注释的数组 [表名, 表注释]
     * @throws SQLException SQL异常
     */
    String[] getTableInfo(ResultSet resultSet) throws SQLException;

    /**
     * 获取列查询语句
     *
     * @param databaseName 数据库名
     * @param tableName    表名
     */
    String getColumnQuery(String databaseName, String tableName);

    /**
     * 从结果集中获取列信息
     *
     * @param resultSet 结果集
     * @return 列信息的JSON字符串
     * @throws SQLException SQL异常
     */
    String getColumnInfo(ResultSet resultSet) throws SQLException;

    /**
     * 获取支持的数据库类型
     */
    String getDatabaseType();

    /**
     * 构建JDBC连接URL（用于查询数据库列表）
     *
     * @param instance 数据库实例（主机:端口）
     */
    String buildDatabaseUrl(String instance);

    /**
     * 构建JDBC连接URL（用于查询表和列信息）
     *
     * @param instance     数据库实例（主机:端口）
     * @param databaseName 数据库名
     */
    String buildTableUrl(String instance, String databaseName);
}
