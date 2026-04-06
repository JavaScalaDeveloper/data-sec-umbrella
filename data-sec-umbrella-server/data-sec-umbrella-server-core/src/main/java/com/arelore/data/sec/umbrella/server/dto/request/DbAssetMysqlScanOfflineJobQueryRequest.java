package com.arelore.data.sec.umbrella.server.dto.request;

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
}
