package com.arelore.data.sec.umbrella.server.core.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DbAssetMysqlScanOfflineJobInstanceQueryRequest extends PageRequest {

    private String taskName;

    /** waiting / running / stopped / completed / failed */
    private String runStatus;
}
