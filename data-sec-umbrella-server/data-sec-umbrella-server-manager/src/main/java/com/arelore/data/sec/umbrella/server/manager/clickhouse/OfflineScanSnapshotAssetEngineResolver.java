package com.arelore.data.sec.umbrella.server.manager.clickhouse;

import com.alibaba.fastjson2.JSON;
import com.arelore.data.sec.umbrella.server.core.constant.OfflineScanJobDatabaseType;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.DbAssetScanOfflineJobInstance;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 从任务实例解析快照写入 ClickHouse 时使用的 engine（与 Worker MQ 消息中 engine 一致）。
 * <p>
 * 优先 {@code database_type} 列；其次 {@code extend_info.assetEngine}；缺省为 MySQL。
 */
public final class OfflineScanSnapshotAssetEngineResolver {

    private OfflineScanSnapshotAssetEngineResolver() {
    }

    public static String resolve(DbAssetScanOfflineJobInstance inst) {
        if (inst != null && StringUtils.hasText(inst.getDatabaseType())) {
            if (OfflineScanJobDatabaseType.CLICKHOUSE.equals(OfflineScanJobDatabaseType.normalizeInstance(inst.getDatabaseType()))) {
                return "Clickhouse";
            }
            return "MySQL";
        }
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
