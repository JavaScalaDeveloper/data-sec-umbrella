package com.arelore.data.sec.umbrella.server.core.dto.request;

import java.util.List;

/**
 * 单条资产/元数据样例，用于策略规则检测（库、表、列及列值等）。
 */
public class DatabasePolicyAssetSample {

    private String databaseName;
    private String databaseDescription;
    private String tableName;
    private String tableDescription;
    private String columnName;
    private String columnDescription;

    /** 该列上采集到的原始值列表（可多条）。 */
    private List<String> columnValues;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseDescription() {
        return databaseDescription;
    }

    public void setDatabaseDescription(String databaseDescription) {
        this.databaseDescription = databaseDescription;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableDescription() {
        return tableDescription;
    }

    public void setTableDescription(String tableDescription) {
        this.tableDescription = tableDescription;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnDescription() {
        return columnDescription;
    }

    public void setColumnDescription(String columnDescription) {
        this.columnDescription = columnDescription;
    }

    public List<String> getColumnValues() {
        return columnValues;
    }

    public void setColumnValues(List<String> columnValues) {
        this.columnValues = columnValues;
    }
}
