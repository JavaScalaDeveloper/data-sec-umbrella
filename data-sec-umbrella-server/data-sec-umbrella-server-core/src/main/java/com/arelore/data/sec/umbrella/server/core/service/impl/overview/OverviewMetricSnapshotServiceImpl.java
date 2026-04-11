package com.arelore.data.sec.umbrella.server.core.service.impl.overview;

import com.arelore.data.sec.umbrella.server.core.entity.mysql.OverviewMetricSnapshot;
import com.arelore.data.sec.umbrella.server.core.mapper.OverviewMetricSnapshotMapper;
import com.arelore.data.sec.umbrella.server.core.service.OverviewMetricSnapshotService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 概览指标快照服务实现。
 *
 * @author 黄佳豪
 */
@Service
public class OverviewMetricSnapshotServiceImpl extends ServiceImpl<OverviewMetricSnapshotMapper, OverviewMetricSnapshot>
        implements OverviewMetricSnapshotService {

    @Override
    public boolean upsert(OverviewMetricSnapshot row) {
        return this.baseMapper.upsert(row) > 0;
    }

    @Override
    public void upsertBatch(List<OverviewMetricSnapshot> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        for (OverviewMetricSnapshot row : rows) {
            this.baseMapper.upsert(row);
        }
    }

    @Override
    public List<OverviewMetricSnapshot> listByPeriodTimeAndCodes(String metricPeriod, String metricTime, List<String> metricCodes) {
        LambdaQueryWrapper<OverviewMetricSnapshot> qw = new LambdaQueryWrapper<>();
        qw.eq(OverviewMetricSnapshot::getMetricPeriod, metricPeriod)
                .eq(OverviewMetricSnapshot::getMetricTime, metricTime);
        if (!CollectionUtils.isEmpty(metricCodes)) {
            qw.in(OverviewMetricSnapshot::getMetricCode, metricCodes);
        }
        return this.list(qw);
    }
}

