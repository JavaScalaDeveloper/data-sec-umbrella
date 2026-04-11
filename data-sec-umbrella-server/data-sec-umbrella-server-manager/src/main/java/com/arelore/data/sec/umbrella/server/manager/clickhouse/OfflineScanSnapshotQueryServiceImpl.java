package com.arelore.data.sec.umbrella.server.manager.clickhouse;

import com.arelore.data.sec.umbrella.server.core.dto.request.OfflineScanInstanceSnapshotDetailRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.OfflineScanSnapshotColumnRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.OfflineScanSnapshotTableRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.OfflineScanSnapshotDetailResponse;
import com.arelore.data.sec.umbrella.server.core.entity.clickhouse.OfflineScanSnapshotColumnEntity;
import com.arelore.data.sec.umbrella.server.core.entity.clickhouse.OfflineScanSnapshotTableEntity;
import com.arelore.data.sec.umbrella.server.manager.mapper.clickhouse.OfflineScanSnapshotColumnMapper;
import com.arelore.data.sec.umbrella.server.manager.mapper.clickhouse.OfflineScanSnapshotTableMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 使用 MyBatis-Plus + ClickHouse 数据源查询离线扫描快照。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "clickhouse.enabled", havingValue = "true")
public class OfflineScanSnapshotQueryServiceImpl implements OfflineScanSnapshotQueryService {

    private static final int MAX_KEY_LEN = 512;
    private static final int MAX_LEVEL_VALUES = 32;

    private final OfflineScanSnapshotTableMapper tableMapper;
    private final OfflineScanSnapshotColumnMapper columnMapper;

    @Override
    public OfflineScanSnapshotDetailResponse query(OfflineScanInstanceSnapshotDetailRequest request) {
        try {
            OfflineScanSnapshotTableRequest tq = request.buildTableQuery();
            OfflineScanSnapshotColumnRequest cq = request.buildColumnQuery();

            Page<OfflineScanSnapshotTableEntity> tablePage = new Page<>(tq.getCurrent(), tq.getSize());
            tableMapper.selectPage(tablePage, buildTableWrapper(tq));

            Page<OfflineScanSnapshotColumnEntity> columnPage = new Page<>(cq.getCurrent(), cq.getSize());
            columnMapper.selectPage(columnPage, buildColumnWrapper(cq));

            OfflineScanSnapshotDetailResponse resp = new OfflineScanSnapshotDetailResponse();
            resp.setTableTotal(tablePage.getTotal());
            resp.setColumnTotal(columnPage.getTotal());
            resp.setTableSnapshots(tablePage.getRecords().stream().map(this::toTableRow).collect(Collectors.toList()));
            resp.setColumnSnapshots(columnPage.getRecords().stream().map(this::toColumnRow).collect(Collectors.toList()));
            return resp;
        } catch (Exception ex) {
            log.warn("clickhouse snapshot query failed, instanceId={}", request.getId(), ex);
            throw new IllegalStateException("ClickHouse 查询失败：" + ex.getMessage());
        }
    }

    private LambdaQueryWrapper<OfflineScanSnapshotTableEntity> buildTableWrapper(OfflineScanSnapshotTableRequest q) {
        LambdaQueryWrapper<OfflineScanSnapshotTableEntity> w = new LambdaQueryWrapper<>();
        w.eq(OfflineScanSnapshotTableEntity::getEngine, q.getEngine())
                .eq(OfflineScanSnapshotTableEntity::getScanKind, q.getScanKind())
                .eq(OfflineScanSnapshotTableEntity::getInstanceId, q.getInstanceId());
        if (StringUtils.hasText(q.getUniqueKeyContains())) {
            w.apply("positionCaseInsensitive(unique_key, {0}) > 0", trimMax(q.getUniqueKeyContains().trim(), MAX_KEY_LEN));
        }
        List<String> levels = normalizeLevels(q.getSensitivityLevels());
        if (!levels.isEmpty()) {
            w.in(OfflineScanSnapshotTableEntity::getSensitivityLevel, levels);
        }
        if (StringUtils.hasText(q.getSensitivityTagsContains())) {
            w.apply("arrayExists(t -> positionCaseInsensitive(t, {0}) > 0, sensitivity_tags)",
                    trimMax(q.getSensitivityTagsContains().trim(), MAX_KEY_LEN));
        }
        w.orderByDesc(OfflineScanSnapshotTableEntity::getEventTime);
        return w;
    }

    private LambdaQueryWrapper<OfflineScanSnapshotColumnEntity> buildColumnWrapper(OfflineScanSnapshotColumnRequest q) {
        LambdaQueryWrapper<OfflineScanSnapshotColumnEntity> w = new LambdaQueryWrapper<>();
        w.eq(OfflineScanSnapshotColumnEntity::getEngine, q.getEngine())
                .eq(OfflineScanSnapshotColumnEntity::getScanKind, q.getScanKind())
                .eq(OfflineScanSnapshotColumnEntity::getInstanceId, q.getInstanceId());
        if (StringUtils.hasText(q.getUniqueKeyContains())) {
            w.apply("positionCaseInsensitive(unique_key, {0}) > 0", trimMax(q.getUniqueKeyContains().trim(), MAX_KEY_LEN));
        }
        List<String> levels = normalizeLevels(q.getSensitivityLevels());
        if (!levels.isEmpty()) {
            w.in(OfflineScanSnapshotColumnEntity::getSensitivityLevel, levels);
        }
        if (StringUtils.hasText(q.getSensitivityTagsContains())) {
            w.apply("arrayExists(t -> positionCaseInsensitive(t, {0}) > 0, sensitivity_tags)",
                    trimMax(q.getSensitivityTagsContains().trim(), MAX_KEY_LEN));
        }
        w.orderByDesc(OfflineScanSnapshotColumnEntity::getEventTime);
        return w;
    }

    private OfflineScanSnapshotDetailResponse.OfflineScanSnapshotTableRow toTableRow(OfflineScanSnapshotTableEntity e) {
        OfflineScanSnapshotDetailResponse.OfflineScanSnapshotTableRow r =
                new OfflineScanSnapshotDetailResponse.OfflineScanSnapshotTableRow();
        BeanUtils.copyProperties(e, r);
        if (r.getSensitivityTags() == null) {
            r.setSensitivityTags(List.of());
        }
        if (r.getColumnDetails() == null) {
            r.setColumnDetails("");
        }
        return r;
    }

    private OfflineScanSnapshotDetailResponse.OfflineScanSnapshotColumnRow toColumnRow(OfflineScanSnapshotColumnEntity e) {
        OfflineScanSnapshotDetailResponse.OfflineScanSnapshotColumnRow r =
                new OfflineScanSnapshotDetailResponse.OfflineScanSnapshotColumnRow();
        BeanUtils.copyProperties(e, r);
        if (r.getSensitivityTags() == null) {
            r.setSensitivityTags(List.of());
        }
        if (r.getSamples() == null) {
            r.setSamples(List.of());
        }
        if (r.getSensitiveSamples() == null) {
            r.setSensitiveSamples(List.of());
        }
        return r;
    }

    private static List<String> normalizeLevels(List<String> sensitivityLevels) {
        if (sensitivityLevels == null || sensitivityLevels.isEmpty()) {
            return List.of();
        }
        return sensitivityLevels.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .limit(MAX_LEVEL_VALUES)
                .collect(Collectors.toList());
    }

    private static String trimMax(String s, int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max);
    }
}
