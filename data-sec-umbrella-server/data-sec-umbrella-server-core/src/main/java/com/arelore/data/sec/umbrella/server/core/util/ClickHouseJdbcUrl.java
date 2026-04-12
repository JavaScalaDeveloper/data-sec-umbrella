package com.arelore.data.sec.umbrella.server.core.util;

import org.springframework.util.StringUtils;

/**
 * ClickHouse JDBC URL 拼装（与官方驱动查询参数一致）。
 * <p>
 * 默认关闭压缩：部分环境未带 LZ4 等 native 依赖时会报
 * {@code LZ4 is not supported ... compress=0}。
 */
public final class ClickHouseJdbcUrl {

    private ClickHouseJdbcUrl() {
    }

    public static String build(String instance, String database) {
        String inst = instance == null ? "" : instance.trim();
        String db = StringUtils.hasText(database) ? database.trim() : "default";
        return "jdbc:clickhouse://" + inst + "/" + db + "?compress=0";
    }
}
