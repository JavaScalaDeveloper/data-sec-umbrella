package com.arelore.data.sec.umbrella.server.core.service.impl.dataasset;

import com.arelore.data.sec.umbrella.server.core.constant.OfflineScanJobDatabaseType;
import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetScanOfflineJobInstanceQueryRequest;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.DbAssetScanOfflineJobInstance;
import com.arelore.data.sec.umbrella.server.core.enums.OfflineJobRunStatusEnum;
import com.arelore.data.sec.umbrella.server.core.mapper.DbAssetScanOfflineJobInstanceMapper;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetScanOfflineJobInstanceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DbAssetScanOfflineJobInstanceServiceImpl
        extends ServiceImpl<DbAssetScanOfflineJobInstanceMapper, DbAssetScanOfflineJobInstance>
        implements DbAssetScanOfflineJobInstanceService {

    @Override
    public List<DbAssetScanOfflineJobInstance> listWaitingInstances() {
        LambdaQueryWrapper<DbAssetScanOfflineJobInstance> qw = new LambdaQueryWrapper<>();
        qw.eq(DbAssetScanOfflineJobInstance::getRunStatus, OfflineJobRunStatusEnum.WAITING.getValue());
        qw.orderByAsc(DbAssetScanOfflineJobInstance::getId);
        return this.list(qw);
    }

    @Override
    public IPage<DbAssetScanOfflineJobInstance> pageQuery(DbAssetScanOfflineJobInstanceQueryRequest request) {
        long current = request.getCurrent() != null ? request.getCurrent() : 1L;
        long size = request.getSize() != null ? request.getSize() : 10L;
        Page<DbAssetScanOfflineJobInstance> page = new Page<>(current, size);
        LambdaQueryWrapper<DbAssetScanOfflineJobInstance> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getTaskName())) {
            w.like(DbAssetScanOfflineJobInstance::getTaskName, request.getTaskName().trim());
        }
        if (StringUtils.hasText(request.getRunStatus())) {
            w.eq(DbAssetScanOfflineJobInstance::getRunStatus, request.getRunStatus().trim());
        }
        applyInstanceDatabaseTypeFilter(w, request.getDatabaseType());
        w.orderByDesc(DbAssetScanOfflineJobInstance::getId);
        return this.page(page, w);
    }

    private void applyInstanceDatabaseTypeFilter(LambdaQueryWrapper<DbAssetScanOfflineJobInstance> w, String databaseType) {
        if (!StringUtils.hasText(databaseType)) {
            return;
        }
        String norm = OfflineScanJobDatabaseType.normalizeInstance(databaseType);
        if (OfflineScanJobDatabaseType.MYSQL.equals(norm)) {
            w.and(q -> q.eq(DbAssetScanOfflineJobInstance::getDatabaseType, OfflineScanJobDatabaseType.MYSQL)
                    .or().isNull(DbAssetScanOfflineJobInstance::getDatabaseType));
        } else {
            w.eq(DbAssetScanOfflineJobInstance::getDatabaseType, OfflineScanJobDatabaseType.CLICKHOUSE);
        }
    }
}
