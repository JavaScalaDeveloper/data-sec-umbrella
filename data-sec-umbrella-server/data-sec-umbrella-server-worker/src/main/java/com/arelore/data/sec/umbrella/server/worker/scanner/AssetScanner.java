package com.arelore.data.sec.umbrella.server.worker.scanner;

import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineMysqlScanDispatchPayload;

import java.util.Map;

/**
 * 资产扫描器接口，按数据库类型路由到具体实现。
 *
 * @author 黄佳豪
 */
public interface AssetScanner {
    /**
     * 当前扫描器支持的数据库类型。
     *
     * @return 数据库类型，例如 MySQL / Clickhouse
     */
    String databaseType();

    /**
     * 执行单个资产扫描。
     *
     * @param payload 扫描任务载荷
     * @param asset 单个资产快照
     * @return 扫描结果（是否敏感）
     */
    AssetScanResult scan(OfflineMysqlScanDispatchPayload payload, Map<String, Object> asset);
}

