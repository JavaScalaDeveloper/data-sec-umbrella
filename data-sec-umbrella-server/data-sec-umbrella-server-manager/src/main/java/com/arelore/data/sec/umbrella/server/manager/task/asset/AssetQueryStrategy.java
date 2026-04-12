package com.arelore.data.sec.umbrella.server.manager.task.asset;

import com.arelore.data.sec.umbrella.server.core.entity.mysql.DbAssetScanOfflineJob;

public interface AssetQueryStrategy {

    /**
     * 资产总数（用于 expected_total）
     */
    long total(DbAssetScanOfflineJob job);

    /**
     * 分页拉取资产详情（每条资产将被拆成一个 MQ msg）
     */
    AssetPage page(DbAssetScanOfflineJob job, long current, long size);
}

