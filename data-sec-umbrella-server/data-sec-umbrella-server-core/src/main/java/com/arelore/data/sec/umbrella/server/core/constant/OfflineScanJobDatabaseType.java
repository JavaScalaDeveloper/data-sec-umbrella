package com.arelore.data.sec.umbrella.server.core.constant;

import org.springframework.util.StringUtils;

/**
 * 离线资产扫描任务配置的「数据库产品」类型（与 database_policy.database_type 命名对齐：MySQL / Clickhouse）。
 */
public final class OfflineScanJobDatabaseType {

    public static final String MYSQL = "MySQL";
    public static final String CLICKHOUSE = "Clickhouse";

    private OfflineScanJobDatabaseType() {
    }

    /**
     * 库中 NULL 视为历史 MySQL 任务。
     */
    public static String normalizeJob(String raw) {
        if (!StringUtils.hasText(raw)) {
            return MYSQL;
        }
        String t = raw.trim();
        if (CLICKHOUSE.equalsIgnoreCase(t) || "ClickHouse".equalsIgnoreCase(t)) {
            return CLICKHOUSE;
        }
        return MYSQL;
    }

    public static String normalizeInstance(String raw) {
        return normalizeJob(raw);
    }
}
