package com.arelore.data.sec.umbrella.server.manager.task.asset;

import com.arelore.data.sec.umbrella.server.core.entity.DbAssetMysqlScanOfflineJob;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Clickhouse 资产查询策略（占位）：待接入 clickhouse 表资产实体/服务后实现。
 */
@Component
public class ClickhouseTableAssetQueryStrategy implements AssetQueryStrategy {

    @Override
    public long total(DbAssetMysqlScanOfflineJob job) {
        return 0;
    }

    @Override
    public AssetPage page(DbAssetMysqlScanOfflineJob job, long current, long size) {
        return new AssetPage(0, List.of());
    }
}

