package com.arelore.data.sec.umbrella.server.core.service.impl.dataasset;

import com.arelore.data.sec.umbrella.server.core.constant.OfflineScanJobDatabaseType;
import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetMysqlScanOfflineJobInstanceQueryRequest;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.DbAssetMysqlScanOfflineJobInstance;
import com.arelore.data.sec.umbrella.server.core.enums.OfflineJobRunStatusEnum;
import com.arelore.data.sec.umbrella.server.core.mapper.DbAssetMysqlScanOfflineJobInstanceMapper;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetMysqlScanOfflineJobInstanceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DbAssetMysqlScanOfflineJobInstanceServiceImpl
        extends ServiceImpl<DbAssetMysqlScanOfflineJobInstanceMapper, DbAssetMysqlScanOfflineJobInstance>
        implements DbAssetMysqlScanOfflineJobInstanceService {

    @Override
    public List<DbAssetMysqlScanOfflineJobInstance> listWaitingInstances() {
        LambdaQueryWrapper<DbAssetMysqlScanOfflineJobInstance> qw = new LambdaQueryWrapper<>();
        qw.eq(DbAssetMysqlScanOfflineJobInstance::getRunStatus, OfflineJobRunStatusEnum.WAITING.getValue());
        qw.orderByAsc(DbAssetMysqlScanOfflineJobInstance::getId);
        return this.list(qw);
    }

    @Override
    public IPage<DbAssetMysqlScanOfflineJobInstance> pageQuery(DbAssetMysqlScanOfflineJobInstanceQueryRequest request) {
        long current = request.getCurrent() != null ? request.getCurrent() : 1L;
        long size = request.getSize() != null ? request.getSize() : 10L;
        Page<DbAssetMysqlScanOfflineJobInstance> page = new Page<>(current, size);
        LambdaQueryWrapper<DbAssetMysqlScanOfflineJobInstance> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getTaskName())) {
            w.like(DbAssetMysqlScanOfflineJobInstance::getTaskName, request.getTaskName().trim());
        }
        if (StringUtils.hasText(request.getRunStatus())) {
            w.eq(DbAssetMysqlScanOfflineJobInstance::getRunStatus, request.getRunStatus().trim());
        }
        applyInstanceDatabaseTypeFilter(w, request.getDatabaseType());
        w.orderByDesc(DbAssetMysqlScanOfflineJobInstance::getId);
        return this.page(page, w);
    }

    private void applyInstanceDatabaseTypeFilter(LambdaQueryWrapper<DbAssetMysqlScanOfflineJobInstance> w, String databaseType) {
        if (!StringUtils.hasText(databaseType)) {
            return;
        }
        String norm = OfflineScanJobDatabaseType.normalizeInstance(databaseType);
        if (OfflineScanJobDatabaseType.MYSQL.equals(norm)) {
            w.and(q -> q.eq(DbAssetMysqlScanOfflineJobInstance::getDatabaseType, OfflineScanJobDatabaseType.MYSQL)
                    .or().isNull(DbAssetMysqlScanOfflineJobInstance::getDatabaseType));
        } else {
            w.eq(DbAssetMysqlScanOfflineJobInstance::getDatabaseType, OfflineScanJobDatabaseType.CLICKHOUSE);
        }
    }
}
