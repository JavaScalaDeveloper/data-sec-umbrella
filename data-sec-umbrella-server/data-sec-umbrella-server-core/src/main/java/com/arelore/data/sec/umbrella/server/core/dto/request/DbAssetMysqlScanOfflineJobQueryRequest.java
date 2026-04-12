package com.arelore.data.sec.umbrella.server.core.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DbAssetMysqlScanOfflineJobQueryRequest extends PageRequest {

    private Long id;

    /**
     * 任务名（模糊）
     */
    private String taskName;

    /**
     * 引擎过滤：MySQL / Clickhouse（由 Controller 按路径写入，用于列表隔离）
     */
    private String databaseType;
}
