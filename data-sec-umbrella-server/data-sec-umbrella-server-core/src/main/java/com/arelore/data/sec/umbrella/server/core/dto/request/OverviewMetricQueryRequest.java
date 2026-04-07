package com.arelore.data.sec.umbrella.server.core.dto.request;

import lombok.Data;

/**
 * 概览指标查询请求。
 *
 * @author 黄佳豪
 */
@Data
public class OverviewMetricQueryRequest {

    /**
     * 数据库类型：MySQL / Clickhouse
     */
    private String databaseType;

    /**
     * 周期：DAY / WEEK / MONTH
     */
    private String metricPeriod;

    /**
     * 统计时间：DAY=yyyyMMdd，MONTH=yyyyMM
     */
    private String metricTime;
}

