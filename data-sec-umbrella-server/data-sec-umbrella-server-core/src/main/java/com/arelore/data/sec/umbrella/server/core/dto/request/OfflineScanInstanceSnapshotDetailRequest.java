package com.arelore.data.sec.umbrella.server.core.dto.request;

import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * 管理端按任务实例查询 ClickHouse 快照。
 * <p>
 * {@link #scanKind} 必填（RULE / AI）；{@link #engine} 由服务端根据实例解析后写入，前端勿传。
 */
@Data
public class OfflineScanInstanceSnapshotDetailRequest {

    private static final String[] IGNORE_WHEN_COPY_TO_SNAPSHOT_QUERY = {
            "id", "tableCurrent", "tableSize", "columnCurrent", "columnSize"
    };

    /** 任务实例 ID */
    private Long id;
    /** RULE 或 AI，与 MQ 消息中 scanKind 一致 */
    private String scanKind;

    /**
     * 资产引擎（如 MySQL），由管理端在调用快照查询前根据实例解析并赋值。
     */
    private String engine;

    /** 唯一键模糊：子串匹配（不区分大小写） */
    private String uniqueKeyContains;

    /** 敏感等级多选，命中 sensitivity_level IN (...) */
    private List<String> sensitivityLevels;

    /** 敏感标签模糊：在任一标签上做子串匹配（不区分大小写） */
    private String sensitivityTagsContains;

    /** 表级快照页码，从 1 开始；缺省 1 */
    private Integer tableCurrent;
    /** 表级每页条数；缺省 10，最大 200 */
    private Integer tableSize;
    /** 字段级快照页码，从 1 开始；缺省 1 */
    private Integer columnCurrent;
    /** 字段级每页条数；缺省 10，最大 200 */
    private Integer columnSize;

    /**
     * 组装表级快照 MyBatis 分页查询参数。
     */
    public OfflineScanSnapshotTableRequest buildTableQuery() {
        OfflineScanSnapshotTableRequest q = new OfflineScanSnapshotTableRequest();
        BeanUtils.copyProperties(this, q, IGNORE_WHEN_COPY_TO_SNAPSHOT_QUERY);
        q.setInstanceId(id);
        q.setUniqueKeyContains(trimToNull(uniqueKeyContains));
        q.setSensitivityLevels(sensitivityLevels);
        q.setSensitivityTagsContains(trimToNull(sensitivityTagsContains));
        q.setCurrent(normalizePage(tableCurrent));
        q.setSize(normalizeSize(tableSize));
        return q;
    }

    /**
     * 组装字段级快照 MyBatis 分页查询参数。
     */
    public OfflineScanSnapshotColumnRequest buildColumnQuery() {
        OfflineScanSnapshotColumnRequest q = new OfflineScanSnapshotColumnRequest();
        BeanUtils.copyProperties(this, q, IGNORE_WHEN_COPY_TO_SNAPSHOT_QUERY);
        q.setInstanceId(id);
        q.setUniqueKeyContains(trimToNull(uniqueKeyContains));
        q.setSensitivityLevels(sensitivityLevels);
        q.setSensitivityTagsContains(trimToNull(sensitivityTagsContains));
        q.setCurrent(normalizePage(columnCurrent));
        q.setSize(normalizeSize(columnSize));
        return q;
    }

    private static String trimToNull(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        return t.isEmpty() ? null : t;
    }

    private static int normalizePage(Integer v) {
        if (v == null || v < 1) {
            return 1;
        }
        return Math.min(v, 1_000_000);
    }

    private static int normalizeSize(Integer v) {
        int def = 10;
        int max = 200;
        if (v == null || v < 1) {
            return def;
        }
        return Math.min(max, v);
    }
}
