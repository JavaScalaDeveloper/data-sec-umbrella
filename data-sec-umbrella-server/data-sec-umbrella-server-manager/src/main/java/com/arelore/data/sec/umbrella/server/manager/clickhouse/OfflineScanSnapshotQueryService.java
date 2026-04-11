package com.arelore.data.sec.umbrella.server.manager.clickhouse;

import com.arelore.data.sec.umbrella.server.core.dto.request.OfflineScanInstanceSnapshotDetailRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.OfflineScanSnapshotDetailResponse;

/**
 * 离线扫描 ClickHouse 快照查询（管理端）。
 */
public interface OfflineScanSnapshotQueryService {

    /**
     * 分页查询表级 + 字段级快照；入参须已包含 {@link OfflineScanInstanceSnapshotDetailRequest#getId()}、
     * {@link OfflineScanInstanceSnapshotDetailRequest#getEngine()}、规范化后的 {@link OfflineScanInstanceSnapshotDetailRequest#getScanKind()} 等。
     */
    OfflineScanSnapshotDetailResponse query(OfflineScanInstanceSnapshotDetailRequest request);
}
