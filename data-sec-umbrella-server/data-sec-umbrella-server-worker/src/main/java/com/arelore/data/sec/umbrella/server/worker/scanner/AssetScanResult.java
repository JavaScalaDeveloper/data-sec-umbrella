package com.arelore.data.sec.umbrella.server.worker.scanner;

/**
 * 单资产扫描结果。
 *
 * @param sensitive 是否判定为敏感资产
 * @author 黄佳豪
 */
public record AssetScanResult(boolean sensitive) {
}

