package com.arelore.data.sec.umbrella.server.core.dto.response;

import lombok.Data;

import java.util.Map;

/**
 * 概览指标查询响应。
 *
 * @author 黄佳豪
 */
@Data
public class OverviewMetricQueryResponse {

    private String databaseType;

    private String metricPeriod;

    private String metricTime;

    /**
     * key=指标 code，value=指标值
     */
    private Map<String, String> metrics;
}

