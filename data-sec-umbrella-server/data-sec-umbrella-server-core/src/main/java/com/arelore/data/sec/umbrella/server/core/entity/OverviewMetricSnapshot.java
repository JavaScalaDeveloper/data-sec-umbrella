package com.arelore.data.sec.umbrella.server.core.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 通用指标历史实体（用于T+1落库）。
 *
 * @author 黄佳豪
 */
@Data
@TableName("common_metric_history")
public class OverviewMetricSnapshot {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date createTime;

    @TableField(value = "modify_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date modifyTime;

    @TableField("metric_code")
    private String metricCode;

    @TableField("metric_period")
    private String metricPeriod;

    @TableField("metric_time")
    private String metricTime;

    @TableField("metric_value")
    private String metricValue;

    @TableField("description")
    private String description;

    @TableField("extend_info")
    private String extendInfo;
}

