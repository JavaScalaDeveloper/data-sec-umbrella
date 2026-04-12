package com.arelore.data.sec.umbrella.server.core.dto.response;

import com.arelore.data.sec.umbrella.server.core.entity.mysql.DbAssetScanOfflineJob;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DbAssetScanOfflineJobResponse extends DbAssetScanOfflineJob {
}
