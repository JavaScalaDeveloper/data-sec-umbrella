package com.arelore.data.sec.umbrella.server.worker.scanner;

import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyAssetSample;

import java.util.List;

/**
 * 单资产扫描结果。
 *
 * @param sensitive 是否判定为敏感资产
 * @author 黄佳豪
 */
public record AssetScanResult(
        boolean sensitive,
        Long assetId,
        String databaseType,
        Integer maxLevel,
        List<String> tags,
        List<ColumnScanInfoItem> columnScanInfo,
        List<DatabasePolicyAssetSample> samples
) {
    /**
     * 列扫描信息。
     */
    public record ColumnScanInfoItem(
            String columnName,
            String sensitivityLevel,
            List<String> sensitivityTags,
            List<String> sensitiveSamples,
            List<String> samples
    ) {
    }
}

