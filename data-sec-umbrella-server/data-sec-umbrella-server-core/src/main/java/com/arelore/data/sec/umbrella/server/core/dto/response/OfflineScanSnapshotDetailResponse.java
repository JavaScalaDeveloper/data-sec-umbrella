package com.arelore.data.sec.umbrella.server.core.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 管理端：某任务实例在 ClickHouse 中的表级 / 字段级快照查询结果。
 */
@Data
public class OfflineScanSnapshotDetailResponse {

    private List<OfflineScanSnapshotRow> tableSnapshots;
    private List<OfflineScanSnapshotRow> columnSnapshots;

    @Data
    public static class OfflineScanSnapshotRow {
        private String eventTime;
        private Long instanceId;
        private Long jobId;
        private String taskName;
        private Long dispatchVersion;
        private String scanKind;
        private String engine;
        private String uniqueKey;
        private String sensitivityLevel;
        private List<String> sensitivityTags;
        /** 表级：列详情 JSON 数组（与 CH column_details 一致） */
        private String columnDetails;
        /** 字段级：样例列表 */
        private List<String> samples;
        /** 字段级：敏感样例列表 */
        private List<String> sensitiveSamples;
    }
}
