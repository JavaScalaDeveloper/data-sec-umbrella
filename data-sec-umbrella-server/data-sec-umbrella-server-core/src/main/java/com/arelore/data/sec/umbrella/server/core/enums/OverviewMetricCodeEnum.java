package com.arelore.data.sec.umbrella.server.core.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 概览指标 code 枚举。
 *
 * @author 黄佳豪
 */
public enum OverviewMetricCodeEnum {
    MYSQL_POLICY_TOTAL("MYSQL_POLICY_TOTAL", "MySQL策略总数"),
    MYSQL_TASK_TOTAL("MYSQL_TASK_TOTAL", "MySQL任务总数"),
    MYSQL_TASK_ENABLED_TOTAL("MYSQL_TASK_ENABLED_TOTAL", "MySQL任务已启用数"),
    MYSQL_DATASOURCE_TOTAL("MYSQL_DATASOURCE_TOTAL", "MySQL数据源总数"),
    MYSQL_BATCH_TASK_TOTAL("MYSQL_BATCH_TASK_TOTAL", "MySQL批量任务总数"),
    MYSQL_BATCH_TASK_INSTANCE_TOTAL("MYSQL_BATCH_TASK_INSTANCE_TOTAL", "MySQL批量任务实例数(按统计日create_time)"),
    MYSQL_INSTANCE_TOTAL("MYSQL_INSTANCE_TOTAL", "MySQL实例总数"),
    MYSQL_DATABASE_TOTAL("MYSQL_DATABASE_TOTAL", "MySQL数据库总数"),
    MYSQL_TABLE_TOTAL("MYSQL_TABLE_TOTAL", "MySQL表总数"),
    MYSQL_SENSITIVE_TABLE_TOTAL("MYSQL_SENSITIVE_TABLE_TOTAL", "MySQL敏感表总数"),
    MYSQL_SENSITIVE_TABLE_RATIO("MYSQL_SENSITIVE_TABLE_RATIO", "MySQL敏感表占比"),
    MYSQL_SENSITIVE_TABLE_EXCLUDE_IGNORE_TOTAL("MYSQL_SENSITIVE_TABLE_EXCLUDE_IGNORE_TOTAL", "MySQL敏感表总数(排除人工忽略)"),
    MYSQL_SENSITIVE_TABLE_EXCLUDE_IGNORE_RATIO("MYSQL_SENSITIVE_TABLE_EXCLUDE_IGNORE_RATIO", "MySQL敏感表占比(排除人工忽略)"),
    MYSQL_SENSITIVITY_LEVEL_DISTRIBUTION("MYSQL_SENSITIVITY_LEVEL_DISTRIBUTION", "MySQL敏感等级分布JSON"),
    MYSQL_SENSITIVITY_TAG_DISTRIBUTION("MYSQL_SENSITIVITY_TAG_DISTRIBUTION", "MySQL敏感标签分布JSON");

    private final String code;
    private final String description;

    OverviewMetricCodeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 获取 MySQL 概览指标 code 列表。
     */
    public static List<String> mysqlCodes() {
        return Arrays.stream(values())
                .map(OverviewMetricCodeEnum::getCode)
                .filter(code -> code.startsWith("MYSQL_"))
                .collect(Collectors.toList());
    }
}

