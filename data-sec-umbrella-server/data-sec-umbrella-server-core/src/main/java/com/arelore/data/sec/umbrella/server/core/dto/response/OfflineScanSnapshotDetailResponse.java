package com.arelore.data.sec.umbrella.server.core.dto.response;

import com.arelore.data.sec.umbrella.server.core.entity.clickhouse.OfflineScanSnapshotColumnEntity;
import com.arelore.data.sec.umbrella.server.core.entity.clickhouse.OfflineScanSnapshotTableEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 管理端：某任务实例在 ClickHouse 中的表级 / 字段级快照查询结果。
 */
@Data
public class OfflineScanSnapshotDetailResponse {

    private List<OfflineScanSnapshotTableRow> tableSnapshots;
    private List<OfflineScanSnapshotColumnRow> columnSnapshots;
    /** 表级快照总条数（与筛选条件一致） */
    private Long tableTotal;
    /** 字段级快照总条数（与筛选条件一致） */
    private Long columnTotal;

    /**
     * 表级快照行（对应 {@code offline_scan_snapshot_table}）。
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class OfflineScanSnapshotTableRow extends OfflineScanSnapshotTableEntity {
    }

    /**
     * 字段级快照行（对应 {@code offline_scan_snapshot_column}）。
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class OfflineScanSnapshotColumnRow extends OfflineScanSnapshotColumnEntity {
    }
}
