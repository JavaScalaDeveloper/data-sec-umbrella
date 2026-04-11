package com.arelore.data.sec.umbrella.server.manager.clickhouse;

import com.arelore.data.sec.umbrella.server.core.dto.request.OfflineScanInstanceSnapshotDetailRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.OfflineScanSnapshotDetailResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * ClickHouse 未启用时的空实现，避免管理端缺少 Bean。
 */
@Service
@ConditionalOnProperty(name = "clickhouse.enabled", havingValue = "false", matchIfMissing = true)
public class OfflineScanSnapshotQueryServiceNoop implements OfflineScanSnapshotQueryService {

    @Override
    public OfflineScanSnapshotDetailResponse query(OfflineScanInstanceSnapshotDetailRequest request) {
        OfflineScanSnapshotDetailResponse resp = new OfflineScanSnapshotDetailResponse();
        resp.setTableSnapshots(Collections.emptyList());
        resp.setColumnSnapshots(Collections.emptyList());
        resp.setTableTotal(0L);
        resp.setColumnTotal(0L);
        return resp;
    }
}
