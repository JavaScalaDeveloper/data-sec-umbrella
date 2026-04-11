package com.arelore.data.sec.umbrella.server.worker.scanner;

import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineDatabaseScanDispatchPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
/**
 * Clickhouse 资产扫描器（当前为预留实现）。
 *
 * @author 黄佳豪
 */
public class ClickhouseAssetScanner implements AssetScanner {
    /**
     * {@inheritDoc}
     */
    @Override
    public String databaseType() {
        return "Clickhouse";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AssetScanResult scan(OfflineDatabaseScanDispatchPayload payload, Map<String, Object> asset) {
        // 预留 Clickhouse 扫描实现
        log.info("Clickhouse scan placeholder, instanceId={}, asset={}", payload.getInstanceId(), asset);
        return new AssetScanResult(false, null, "Clickhouse", 0, List.of(), List.of(), List.of());
    }
}

