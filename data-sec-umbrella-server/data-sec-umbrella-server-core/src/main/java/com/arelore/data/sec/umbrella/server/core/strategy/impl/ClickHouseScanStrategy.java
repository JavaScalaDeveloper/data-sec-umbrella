package com.arelore.data.sec.umbrella.server.core.strategy.impl;

import com.arelore.data.sec.umbrella.server.core.strategy.DatabaseScanStrategy;
import com.arelore.data.sec.umbrella.server.core.util.ClickHouseJdbcUrl;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ClickHouse 资产扫描（system 库元数据）
 */
public class ClickHouseScanStrategy implements DatabaseScanStrategy {

    private static String escapeSqlString(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("'", "''");
    }

    @Override
    public String getDatabaseQuery() {
        return "SELECT name AS SCHEMA_NAME FROM system.databases "
                + "WHERE name NOT IN ('system', 'information_schema', 'INFORMATION_SCHEMA') "
                + "ORDER BY name";
    }

    @Override
    public String getTableQuery(String databaseName) {
        String db = escapeSqlString(databaseName);
        // 不使用 is_dictionary（部分 24.x 等版本在 SELECT 中不可用），用 engine + 表名排除非业务对象
        return "SELECT name AS TABLE_NAME, coalesce(comment, '') AS TABLE_COMMENT "
                + "FROM system.tables WHERE database = '" + db + "' "
                + "AND name NOT LIKE '.inner%' "
                + "AND name NOT LIKE '.tmp%' "
                + "AND engine NOT IN ("
                + "'Dictionary', 'View', 'Buffer', 'Null', 'Set', 'Join', 'EmbeddedRocksDB'"
                + ") "
                + "ORDER BY name";
    }

    @Override
    public String[] getTableInfo(ResultSet resultSet) throws SQLException {
        String tableName = resultSet.getString("TABLE_NAME");
        String tableComment = resultSet.getString("TABLE_COMMENT");
        return new String[]{tableName, tableComment};
    }

    @Override
    public String getColumnQuery(String databaseName, String tableName) {
        String db = escapeSqlString(databaseName);
        String tb = escapeSqlString(tableName);
        return "SELECT name AS COLUMN_NAME, type AS COLUMN_TYPE, coalesce(comment, '') AS COLUMN_COMMENT, "
                + "if(startsWith(type, 'Nullable'), 'YES', 'NO') AS IS_NULLABLE, "
                + "coalesce(default_expression, '') AS COLUMN_DEFAULT "
                + "FROM system.columns WHERE database = '" + db + "' AND table = '" + tb + "' "
                + "ORDER BY position";
    }

    @Override
    public String getColumnInfo(ResultSet resultSet) throws SQLException {
        JSONArray columnArray = new JSONArray();
        while (resultSet.next()) {
            JSONObject columnObj = new JSONObject();
            columnObj.put("columnName", resultSet.getString("COLUMN_NAME"));
            columnObj.put("columnType", resultSet.getString("COLUMN_TYPE"));
            columnObj.put("columnComment", resultSet.getString("COLUMN_COMMENT"));
            columnObj.put("isNullable", resultSet.getString("IS_NULLABLE"));
            columnObj.put("columnDefault", resultSet.getString("COLUMN_DEFAULT"));
            columnArray.add(columnObj);
        }
        return columnArray.toJSONString();
    }

    @Override
    public String getDatabaseType() {
        return "Clickhouse";
    }

    @Override
    public String buildDatabaseUrl(String instance) {
        return ClickHouseJdbcUrl.build(instance, "default");
    }

    @Override
    public String buildTableUrl(String instance, String databaseName) {
        String db = databaseName == null || databaseName.isBlank() ? "default" : databaseName.trim();
        return ClickHouseJdbcUrl.build(instance, db);
    }
}
