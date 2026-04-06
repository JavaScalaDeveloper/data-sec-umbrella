package com.arelore.data.sec.umbrella.server.core.strategy.impl;

import com.arelore.data.sec.umbrella.server.core.strategy.DatabaseScanStrategy;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MySQL数据库扫描策略实现
 */
public class MySQLScanStrategy implements DatabaseScanStrategy {

    @Override
    public String getDatabaseQuery() {
        return "SELECT SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME FROM SCHEMATA " +
                "WHERE SCHEMA_NAME NOT IN ('information_schema', 'mysql', 'performance_schema', 'sys')";
    }

    @Override
    public String getTableQuery(String databaseName) {
        return "SELECT TABLE_NAME, TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + databaseName + "'";
    }

    @Override
    public String[] getTableInfo(ResultSet resultSet) throws SQLException {
        String tableName = resultSet.getString("TABLE_NAME");
        String tableComment = resultSet.getString("TABLE_COMMENT");
        return new String[]{tableName, tableComment};
    }

    @Override
    public String getColumnQuery(String databaseName, String tableName) {
        return "SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT, IS_NULLABLE, COLUMN_DEFAULT FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = '" + databaseName + "' AND TABLE_NAME = '" + tableName + "'";
    }

    @Override
    public String getColumnInfo(ResultSet resultSet) throws SQLException {
        JSONArray columnArray = new JSONArray();
        while (resultSet.next()) {
            JSONObject columnObj = new JSONObject();
            columnObj.put("columnName", resultSet.getString("COLUMN_NAME"));
            columnObj.put("columnType", resultSet.getString("COLUMN_TYPE"));
            columnObj.put("columnComment", resultSet.getString("COLUMN_COMMENT"));
            columnArray.add(columnObj);
        }
        return columnArray.toJSONString();
    }

    @Override
    public String getDatabaseType() {
        return "MySQL";
    }

    @Override
    public String buildDatabaseUrl(String instance) {
        return "jdbc:mysql://" + instance + "/information_schema?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
    }

    @Override
    public String buildTableUrl(String instance, String databaseName) {
        return "jdbc:mysql://" + instance + "/" + databaseName + "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
    }
}
