package com.arelore.data.sec.umbrella.server.manager.schedule;

import com.arelore.data.sec.umbrella.server.manager.overview.OverviewMetricAggregator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 概览指标定时刷新任务。
 *
 * @author 黄佳豪
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OverviewMetricDailySchedule {

    private final OverviewMetricAggregator overviewMetricAggregator;

    /**
     * 每半小时刷新一次当天概览指标。
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void aggregateCurrentDayMetrics() {
        LocalDate target = LocalDate.now();
        try {
            overviewMetricAggregator.aggregateMysqlDaily(target);
        } catch (Exception ex) {
            log.error("aggregate mysql overview metrics failed, date={}", target, ex);
        }
        try {
            overviewMetricAggregator.aggregateClickhouseDaily(target);
        } catch (Exception ex) {
            log.error("aggregate clickhouse overview metrics failed, date={}", target, ex);
        }
    }
}

