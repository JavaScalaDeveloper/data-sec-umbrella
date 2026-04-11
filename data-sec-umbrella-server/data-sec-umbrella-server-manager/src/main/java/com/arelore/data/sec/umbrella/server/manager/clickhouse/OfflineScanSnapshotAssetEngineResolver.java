package com.arelore.data.sec.umbrella.server.manager.clickhouse;

import com.alibaba.fastjson2.JSON;
import com.arelore.data.sec.umbrella.server.core.entity.DbAssetMysqlScanOfflineJobInstance;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 从任务实例解析快照写入 ClickHouse 时使用的 engine（与 Worker MQ 消息中 engine 一致）。
 * <p>
 * 优先读取实例 {@code extend_info.assetEngine}（由分发任务写入）；缺失时默认 MySQL（本模块为 MySQL 离线扫描）。
 */
public final class OfflineScanSnapshotAssetEngineResolver {

    private OfflineScanSnapshotAssetEngineResolver() {
    }

    public static String resolve(DbAssetMysqlScanOfflineJobInstance inst) {
        if (inst == null || !StringUtils.hasText(inst.getExtendInfo())) {
            return "MySQL";
        }
        try {
            Map<?, ?> m = JSON.parseObject(inst.getExtendInfo(), Map.class);
            if (m == null) {
                return "MySQL";
            }
            Object v = m.get("assetEngine");
            if (v != null && StringUtils.hasText(String.valueOf(v))) {
                return String.valueOf(v).trim();
            }
        } catch (Exception ignored) {
        }
        return "MySQL";
    }
}
