package com.arelore.data.sec.umbrella.server.core.strategy.impl;

import com.arelore.data.sec.umbrella.server.core.strategy.DatabaseScanStrategy;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Oracle数据库扫描策略实现
 */
public class OracleScanStrategy implements DatabaseScanStrategy {

    @Override
    public String getDatabaseQuery() {
        return "SELECT username FROM all_users WHERE username NOT IN ('SYS', 'SYSTEM', 'SYSMAN', 'DBSNMP', 'OUTLN', " +
                "'APPQOSSYS', 'DBSFWUSER', 'GSMADMIN_INTERNAL', 'GSMROOTUSER', 'GSMUSER', 'LBACSYS', 'MDSYS', " +
                "'OLAPSYS', 'ORDDATA', 'ORDPLUGINS', 'ORDSYS', 'OUTLN', 'SI_INFORMTN_SCHEMA', 'SYS', 'SYSBACKUP', " +
                "'SYSDG', 'SYSKM', 'SYSTEM', 'SYSTEM', 'TSMSYS', 'WK_TEST', 'WKPROXY', 'WKSYS', 'WMSYS', 'XDB', 'XS$NULL')";
    }

    @Override
    public String getTableQuery(String databaseName) {
        return "SELECT table_name FROM all_tables WHERE owner = '" + databaseName.toUpperCase() + "'";
    }

    @Override
    public String[] getTableInfo(ResultSet resultSet) throws SQLException {
        String tableName = resultSet.getString("table_name");
        return new String[]{tableName, ""};
    }

    @Override
    public String getColumnQuery(String databaseName, String tableName) {
        return "SELECT column_name, data_type, data_length, nullable FROM all_tab_columns " +
                "WHERE owner = '" + databaseName.toUpperCase() + "' AND table_name = '" + tableName.toUpperCase() + "'";
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
        return "Oracle";
    }

    @Override
    public String buildDatabaseUrl(String instance) {
        return "jdbc:oracle:thin:@" + instance + "/";
    }

    @Override
    public String buildTableUrl(String instance, String databaseName) {
        return "jdbc:oracle:thin:@" + instance + ":" + databaseName;
    }
}
