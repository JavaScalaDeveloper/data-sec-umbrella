package com.arelore.data.sec.umbrella.server.core.enums;

/**
 * 数据库类型枚举
 */
public enum DatabaseSourceTypeEnum {
    /**
     * MySQL数据库
     */
    MYSQL("MySQL", "jdbc:mysql://", "/information_schema?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai"),

    /**
     * Oracle数据库
     */
    ORACLE("Oracle", "jdbc:oracle:thin:@", "/"),

    /**
     * SQL Server数据库
     */
    SQL_SERVER("SQL Server", "jdbc:sqlserver://", ";databaseName=master"),

    /**
     * 默认值
     */
    DEFAULT("MySQL", "jdbc:mysql://", "/information_schema?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai");

    /**
     * 数据库类型名称
     */
    private final String typeName;

    /**
     * JDBC协议前缀
     */
    private final String jdbcPrefix;

    /**
     * JDBC协议后缀
     */
    private final String jdbcSuffix;

    DatabaseSourceTypeEnum(String typeName, String jdbcPrefix, String jdbcSuffix) {
        this.typeName = typeName;
        this.jdbcPrefix = jdbcPrefix;
        this.jdbcSuffix = jdbcSuffix;
    }

    /**
     * 根据数据库类型名称获取枚举值
     *
     * @param typeName 数据库类型名称
     * @return 数据库类型枚举
     */
    public static DatabaseSourceTypeEnum getByTypeName(String typeName) {
        if (typeName == null) {
            return DEFAULT;
        }
        for (DatabaseSourceTypeEnum enumValue : values()) {
            if (enumValue.typeName.equalsIgnoreCase(typeName)) {
                return enumValue;
            }
        }
        return DEFAULT;
    }

    /**
     * 获取JDBC连接URL
     *
     * @param instance 数据库实例（主机:端口）
     * @return JDBC连接URL
     */
    public String getJdbcUrl(String instance) {
        return jdbcPrefix + instance + jdbcSuffix;
    }

    /**
     * 获取JDBC连接URL（带数据库名）
     *
     * @param instance     数据库实例（主机:端口）
     * @param databaseName 数据库名
     * @return JDBC连接URL
     */
    public String getJdbcUrl(String instance, String databaseName) {
        if (this == MYSQL) {
            return jdbcPrefix + instance + "/" + databaseName + "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
        } else if (this == ORACLE) {
            return jdbcPrefix + instance + ":" + databaseName;
        } else if (this == SQL_SERVER) {
            return jdbcPrefix + instance + ";databaseName=" + databaseName;
        }
        return jdbcPrefix + instance + "/" + databaseName;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getJdbcPrefix() {
        return jdbcPrefix;
    }

    public String getJdbcSuffix() {
        return jdbcSuffix;
    }
}
