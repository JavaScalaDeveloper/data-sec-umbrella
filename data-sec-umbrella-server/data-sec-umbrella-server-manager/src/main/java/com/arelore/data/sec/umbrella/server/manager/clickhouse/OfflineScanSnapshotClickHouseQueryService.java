package com.arelore.data.sec.umbrella.server.manager.clickhouse;

import com.arelore.data.sec.umbrella.server.core.dto.response.OfflineScanSnapshotDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 从 ClickHouse 查询离线扫描表级 / 字段级快照（与 scripts/clickhouse 中 DDL 对齐）。
 */
@Slf4j
@Service
public class OfflineScanSnapshotClickHouseQueryService {

    private static final Pattern SAFE_FQTN = Pattern.compile("^[a-zA-Z0-9_.]+$");
    private static final int MAX_KEY_LEN = 512;
    private static final int MAX_LEVEL_VALUES = 32;

    @Value("${clickhouse.enabled:false}")
    private boolean enabled;

    @Value("${clickhouse.jdbc-url:jdbc:ch://localhost:8123/default?compress=0}")
    private String jdbcUrl;

    @Value("${clickhouse.username:default}")
    private String username;

    @Value("${clickhouse.password:}")
    private String password;

    @Value("${clickhouse.snapshot-table:default.offline_scan_snapshot_table}")
    private String snapshotTable;

    @Value("${clickhouse.snapshot-column:default.offline_scan_snapshot_column}")
    private String snapshotColumn;

    @Value("${clickhouse.query-limit:5000}")
    private int queryLimit;

    /**
     * 按实例 + engine + scanKind 查询快照，并支持唯一键子串、等级多选、标签子串过滤。
     */
    public OfflineScanSnapshotDetailResponse query(
            long instanceId,
            String engine,
            String scanKind,
            String uniqueKeyContains,
            List<String> sensitivityLevels,
            String sensitivityTagsContains) {
        OfflineScanSnapshotDetailResponse resp = new OfflineScanSnapshotDetailResponse();
        resp.setTableSnapshots(Collections.emptyList());
        resp.setColumnSnapshots(Collections.emptyList());
        if (!enabled) {
            return resp;
        }
        assertSafeIdentifier(snapshotTable);
        assertSafeIdentifier(snapshotColumn);
        Properties props = new Properties();
        props.setProperty("user", username);
        if (password != null && !password.isEmpty()) {
            props.setProperty("password", password);
        }
        int limit = Math.min(50_000, Math.max(1, queryLimit));
        try (Connection conn = DriverManager.getConnection(jdbcUrl, props)) {
            resp.setTableSnapshots(selectRows(conn, snapshotTable, instanceId, engine, scanKind,
                    uniqueKeyContains, sensitivityLevels, sensitivityTagsContains, limit, true));
            resp.setColumnSnapshots(selectRows(conn, snapshotColumn, instanceId, engine, scanKind,
                    uniqueKeyContains, sensitivityLevels, sensitivityTagsContains, limit, false));
        } catch (Exception ex) {
            log.warn("clickhouse snapshot query failed, instanceId={}", instanceId, ex);
            throw new IllegalStateException("ClickHouse 查询失败：" + ex.getMessage());
        }
        return resp;
    }

    private static void assertSafeIdentifier(String name) {
        if (name == null || !SAFE_FQTN.matcher(name).matches()) {
            throw new IllegalArgumentException("非法的 ClickHouse 表名配置");
        }
    }

    private static List<OfflineScanSnapshotDetailResponse.OfflineScanSnapshotRow> selectRows(
            Connection conn,
            String fqTable,
            long instanceId,
            String engine,
            String scanKind,
            String uniqueKeyContains,
            List<String> sensitivityLevels,
            String sensitivityTagsContains,
            int limit,
            boolean tableLevel) throws SQLException {
        StringBuilder where = new StringBuilder(
                "WHERE engine = ? AND scan_kind = ? AND instance_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(engine);
        params.add(scanKind);
        params.add(instanceId);

        if (StringUtils.hasText(uniqueKeyContains)) {
            where.append("AND positionCaseInsensitive(unique_key, ?) > 0 ");
            params.add(trimMax(uniqueKeyContains.trim(), MAX_KEY_LEN));
        }
        List<String> levels = normalizeLevels(sensitivityLevels);
        if (!levels.isEmpty()) {
            where.append("AND sensitivity_level IN (");
            where.append(levels.stream().map(x -> "?").collect(Collectors.joining(",")));
            where.append(") ");
            params.addAll(levels);
        }
        if (StringUtils.hasText(sensitivityTagsContains)) {
            where.append("AND arrayExists(t -> positionCaseInsensitive(t, ?) > 0, sensitivity_tags) ");
            params.add(trimMax(sensitivityTagsContains.trim(), MAX_KEY_LEN));
        }

        String selectCols = "formatDateTime(event_time, '%Y-%m-%d %H:%i:%s') AS event_time_fmt, "
                + "instance_id, job_id, task_name, dispatch_version, scan_kind, engine, "
                + "unique_key, sensitivity_level, sensitivity_tags";
        if (tableLevel) {
            selectCols += ", column_details";
        } else {
            selectCols += ", samples, sensitive_samples";
        }
        String sql = "SELECT " + selectCols + " FROM " + fqTable + " " + where + " ORDER BY event_time DESC LIMIT ?";
        params.add(limit);

        List<OfflineScanSnapshotDetailResponse.OfflineScanSnapshotRow> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                bind(ps, i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OfflineScanSnapshotDetailResponse.OfflineScanSnapshotRow row =
                            new OfflineScanSnapshotDetailResponse.OfflineScanSnapshotRow();
                    row.setEventTime(rs.getString("event_time_fmt"));
                    row.setInstanceId(rs.getLong("instance_id"));
                    row.setJobId(rs.getLong("job_id"));
                    row.setTaskName(rs.getString("task_name"));
                    row.setDispatchVersion(rs.getLong("dispatch_version"));
                    row.setScanKind(rs.getString("scan_kind"));
                    row.setEngine(rs.getString("engine"));
                    row.setUniqueKey(rs.getString("unique_key"));
                    row.setSensitivityLevel(rs.getString("sensitivity_level"));
                    row.setSensitivityTags(toStringList(rs.getObject("sensitivity_tags")));
                    if (tableLevel) {
                        String cd = rs.getString("column_details");
                        row.setColumnDetails(cd == null ? "" : cd);
                    } else {
                        row.setSamples(toStringList(rs.getObject("samples")));
                        row.setSensitiveSamples(toStringList(rs.getObject("sensitive_samples")));
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private static void bind(PreparedStatement ps, int idx, Object v) throws SQLException {
        if (v instanceof String s) {
            ps.setString(idx, s);
        } else if (v instanceof Long l) {
            ps.setLong(idx, l);
        } else if (v instanceof Integer n) {
            ps.setInt(idx, n);
        } else {
            ps.setObject(idx, v);
        }
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

    @SuppressWarnings("unchecked")
    private static List<String> toStringList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            List<String> out = new ArrayList<>(list.size());
            for (Object o : list) {
                out.add(o == null ? "" : String.valueOf(o));
            }
            return out;
        }
        if (value instanceof String[] arr) {
            return List.of(arr);
        }
        if (value instanceof Array sqlArr) {
            try {
                Object arr = sqlArr.getArray();
                if (arr instanceof Object[] oa) {
                    List<String> out = new ArrayList<>(oa.length);
                    for (Object o : oa) {
                        out.add(o == null ? "" : String.valueOf(o));
                    }
                    return out;
                }
            } catch (SQLException ignored) {
            }
        }
        return List.of(String.valueOf(value));
    }
}
