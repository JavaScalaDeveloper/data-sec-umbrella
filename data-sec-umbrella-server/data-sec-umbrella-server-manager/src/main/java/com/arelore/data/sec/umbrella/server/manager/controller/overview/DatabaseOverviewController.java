package com.arelore.data.sec.umbrella.server.manager.controller.overview;

import com.arelore.data.sec.umbrella.server.core.common.Result;
import com.arelore.data.sec.umbrella.server.core.dto.request.OverviewMetricQueryRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.OverviewMetricQueryResponse;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.OverviewMetricSnapshot;
import com.arelore.data.sec.umbrella.server.core.enums.MetricPeriodEnum;
import com.arelore.data.sec.umbrella.server.core.enums.OverviewMetricCodeEnum;
import com.arelore.data.sec.umbrella.server.core.service.OverviewMetricSnapshotService;
import com.arelore.data.sec.umbrella.server.manager.overview.OverviewMetricAggregator;
import com.arelore.data.sec.umbrella.server.manager.security.AdminPermission;
import com.arelore.data.sec.umbrella.server.manager.security.PermissionAction;
import com.arelore.data.sec.umbrella.server.manager.security.ProductCode;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库安全概览指标查询接口。
 *
 * @author 黄佳豪
 */
@RestController
@RequestMapping("api/overview/database")
@AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.READ)
public class DatabaseOverviewController {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OverviewMetricSnapshotService metricSnapshotService;
    private final OverviewMetricAggregator overviewMetricAggregator;

    public DatabaseOverviewController(OverviewMetricSnapshotService metricSnapshotService,
                                      OverviewMetricAggregator overviewMetricAggregator) {
        this.metricSnapshotService = metricSnapshotService;
        this.overviewMetricAggregator = overviewMetricAggregator;
    }

    /**
     * 查询指定周期/时间的概览指标快照。
     */
    @PostMapping("/metrics")
    public Result<OverviewMetricQueryResponse> metrics(@RequestBody(required = false) OverviewMetricQueryRequest request) {
        String databaseType = request != null && StringUtils.hasText(request.getDatabaseType())
                ? request.getDatabaseType().trim()
                : "MySQL";
        String metricPeriod = request != null && StringUtils.hasText(request.getMetricPeriod())
                ? request.getMetricPeriod().trim().toUpperCase()
                : MetricPeriodEnum.DAY.name();
        String metricTime = request != null && StringUtils.hasText(request.getMetricTime())
                ? request.getMetricTime().trim()
                : DAY_FMT.format(LocalDate.now().minusDays(1));

        if (!MetricPeriodEnum.DAY.name().equals(metricPeriod) && !MetricPeriodEnum.WEEK.name().equals(metricPeriod)
                && !MetricPeriodEnum.MONTH.name().equals(metricPeriod)) {
            return Result.error("metricPeriod非法，支持DAY/WEEK/MONTH");
        }

        List<String> codes;
        if ("MySQL".equalsIgnoreCase(databaseType)) {
            codes = OverviewMetricCodeEnum.mysqlCodes();
        } else {
            codes = List.of();
        }

        List<OverviewMetricSnapshot> rows = metricSnapshotService.listByPeriodTimeAndCodes(metricPeriod, metricTime, codes);
        Map<String, String> metrics = new LinkedHashMap<>();
        for (OverviewMetricSnapshot row : rows) {
            metrics.put(row.getMetricCode(), row.getMetricValue());
        }

        OverviewMetricQueryResponse resp = new OverviewMetricQueryResponse();
        resp.setDatabaseType(databaseType);
        resp.setMetricPeriod(metricPeriod);
        resp.setMetricTime(metricTime);
        resp.setMetrics(metrics);
        return Result.success(resp);
    }

    /**
     * 手动触发概览指标刷新（当前仅支持 MySQL 日级）。
     */
    @PostMapping("/refresh")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> refresh(@RequestBody(required = false) OverviewMetricQueryRequest request) {
        String databaseType = request != null && StringUtils.hasText(request.getDatabaseType())
                ? request.getDatabaseType().trim()
                : "MySQL";
        if (!"MySQL".equalsIgnoreCase(databaseType)) {
            return Result.error("当前仅支持MySQL刷新");
        }
        String metricTime = request != null && StringUtils.hasText(request.getMetricTime())
                ? request.getMetricTime().trim()
                : DAY_FMT.format(LocalDate.now());
        try {
            LocalDate day = LocalDate.parse(metricTime, DAY_FMT);
            overviewMetricAggregator.aggregateMysqlDaily(day);
            return Result.success(true);
        } catch (Exception ex) {
            return Result.error("刷新失败: " + ex.getMessage());
        }
    }
}

