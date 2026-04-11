package com.arelore.data.sec.umbrella.server.core.dto.messaging;

/**
 * 扫描快照在 MQ / 分析库中的业务唯一键拼接规则。
 * <p>
 * MySQL：表级为 {@code 实例,库名,表名}；字段级为 {@code 实例,库名,表名,字段名}（逗号分隔）。
 * 其他引擎暂沿用相同维度，可按调用方传入的 engine 在业务层分支后替换实现。
 */
public final class OfflineScanSnapshotUniqueKey {

    private OfflineScanSnapshotUniqueKey() {
    }

    public static String tableRowKey(String dataInstance, String databaseName, String tableName) {
        return join(dataInstance, databaseName, tableName);
    }

    public static String columnRowKey(String dataInstance, String databaseName, String tableName, String columnName) {
        return join(dataInstance, databaseName, tableName, columnName);
    }

    private static String join(String... parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(parts[i] == null ? "" : parts[i]);
        }
        return sb.toString();
    }
}
