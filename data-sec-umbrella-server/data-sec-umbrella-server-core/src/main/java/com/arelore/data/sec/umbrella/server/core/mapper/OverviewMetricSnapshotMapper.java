package com.arelore.data.sec.umbrella.server.core.mapper;

import com.arelore.data.sec.umbrella.server.core.entity.OverviewMetricSnapshot;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 通用指标历史 Mapper。
 *
 * @author 黄佳豪
 */
@Mapper
public interface OverviewMetricSnapshotMapper extends BaseMapper<OverviewMetricSnapshot> {

    /**
     * 按唯一键（metric_code + metric_period + metric_time）upsert。
     */
    @Insert("INSERT INTO common_metric_history(metric_code, metric_period, metric_time, metric_value, description, extend_info) " +
            "VALUES(#{row.metricCode}, #{row.metricPeriod}, #{row.metricTime}, #{row.metricValue}, #{row.description}, #{row.extendInfo}) " +
            "ON DUPLICATE KEY UPDATE metric_value=VALUES(metric_value), description=VALUES(description), extend_info=VALUES(extend_info)")
    int upsert(@Param("row") OverviewMetricSnapshot row);
}

