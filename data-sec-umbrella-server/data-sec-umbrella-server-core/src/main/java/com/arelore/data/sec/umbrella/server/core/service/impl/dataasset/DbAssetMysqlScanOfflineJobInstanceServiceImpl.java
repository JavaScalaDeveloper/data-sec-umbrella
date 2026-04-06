package com.arelore.data.sec.umbrella.server.core.service.impl.dataasset;

import com.arelore.data.sec.umbrella.server.core.entity.DbAssetMysqlScanOfflineJobInstance;
import com.arelore.data.sec.umbrella.server.core.enums.OfflineJobRunStatusEnum;
import com.arelore.data.sec.umbrella.server.core.mapper.DbAssetMysqlScanOfflineJobInstanceMapper;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetMysqlScanOfflineJobInstanceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
}
