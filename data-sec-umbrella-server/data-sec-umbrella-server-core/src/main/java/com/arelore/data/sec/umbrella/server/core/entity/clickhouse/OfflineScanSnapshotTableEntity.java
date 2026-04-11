package com.arelore.data.sec.umbrella.server.core.entity.clickhouse;

import com.arelore.data.sec.umbrella.server.core.typehandler.ClickHouseStringArrayTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClickHouse 表 {@code offline_scan_snapshot_table}（与 scripts/clickhouse 对齐，只读查询）。
 */
@Data
@TableName(value = "offline_scan_snapshot_table", autoResultMap = true)
public class OfflineScanSnapshotTableEntity {

    @TableField("event_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime eventTime;

    @TableField("instance_id")
    private Long instanceId;

    @TableField("job_id")
    private Long jobId;

    @TableField("task_name")
    private String taskName;

    @TableField("dispatch_version")
    private Long dispatchVersion;

    @TableField("scan_kind")
    private String scanKind;

    @TableField("engine")
    private String engine;

    @TableField("unique_key")
    private String uniqueKey;

    @TableField("sensitivity_level")
    private String sensitivityLevel;

    @TableField(value = "sensitivity_tags", typeHandler = ClickHouseStringArrayTypeHandler.class)
    private List<String> sensitivityTags;

    @TableField("column_details")
    private String columnDetails;
}
