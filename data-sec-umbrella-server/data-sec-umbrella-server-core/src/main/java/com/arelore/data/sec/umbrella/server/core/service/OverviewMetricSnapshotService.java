package com.arelore.data.sec.umbrella.server.core.service;

import com.arelore.data.sec.umbrella.server.core.entity.mysql.OverviewMetricSnapshot;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 概览指标快照服务。
 *
 * @author 黄佳豪
 */
public interface OverviewMetricSnapshotService extends IService<OverviewMetricSnapshot> {

    /**
     * 写入或更新单个指标快照。
     */
    boolean upsert(OverviewMetricSnapshot row);

    /**
     * 批量写入或更新指标快照。
     */
    void upsertBatch(List<OverviewMetricSnapshot> rows);

    /**
     * 按周期+时间+指标 code 列表查询。
     */
    List<OverviewMetricSnapshot> listByPeriodTimeAndCodes(String metricPeriod, String metricTime, List<String> metricCodes);
}

