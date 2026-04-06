package com.arelore.data.sec.umbrella.server.core.strategy.impl;

import com.arelore.data.sec.umbrella.server.core.strategy.DatabaseScanStrategy;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SQL Server数据库扫描策略实现
 */
public class SQLServerScanStrategy implements DatabaseScanStrategy {

    @Override
    public String getDatabaseQuery() {
        return "SELECT name FROM sys.databases WHERE name NOT IN ('master', 'tempdb', 'model', 'msdb')";
    }

    @Override
    public String getTableQuery(String databaseName) {
        return "SELECT name FROM sys.tables WHERE type = 'U'";
    }

    @Override
    public String[] getTableInfo(ResultSet resultSet) throws SQLException {
        String tableName = resultSet.getString("name");
        return new String[]{tableName, ""};
    }

    @Override
    public String getColumnQuery(String databaseName, String tableName) {
        return "SELECT c.name AS column_name, t.name AS data_type, c.max_length, c.is_nullable FROM sys.columns c " +
                "JOIN sys.types t ON c.system_type_id = t.system_type_id " +
                "WHERE c.object_id = OBJECT_ID('" + databaseName + ".dbo." + tableName + "')";
    }

    @Override
    public String getColumnInfo(ResultSet resultSet) throws SQLException {
        JSONArray columnArray = new JSONArray();
        while (resultSet.next()) {
            JSONObject columnObj = new JSONObject();
            columnObj.put("columnName", resultSet.getString("column_name"));
            columnObj.put("columnType", resultSet.getString("data_type"));
            columnObj.put("columnComment", "");
            columnArray.add(columnObj);
        }
        return columnArray.toJSONString();
    }

    @Override
    public String getDatabaseType() {
        return "SQL Server";
    }

    @Override
    public String buildDatabaseUrl(String instance) {
        return "jdbc:sqlserver://" + instance + ";databaseName=master";
    }

    @Override
    public String buildTableUrl(String instance, String databaseName) {
        return "jdbc:sqlserver://" + instance + ";databaseName=" + databaseName;
    }
}
