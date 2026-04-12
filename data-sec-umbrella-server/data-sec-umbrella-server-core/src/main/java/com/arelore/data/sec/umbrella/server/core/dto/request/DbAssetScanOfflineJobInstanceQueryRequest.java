package com.arelore.data.sec.umbrella.server.core.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DbAssetScanOfflineJobInstanceQueryRequest extends PageRequest {

    private String taskName;

    /** waiting / running / stopped / completed / failed */
    private String runStatus;

    /**
     * 引擎过滤：MySQL / Clickhouse（由 Controller 按路径写入）
     */
    private String databaseType;
}
