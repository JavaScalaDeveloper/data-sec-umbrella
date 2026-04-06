package com.arelore.data.sec.umbrella.server.service.impl.dataasset;

import com.arelore.data.sec.umbrella.server.entity.DbAssetMysqlScanOfflineJobInstance;
import com.arelore.data.sec.umbrella.server.mapper.DbAssetMysqlScanOfflineJobInstanceMapper;
import com.arelore.data.sec.umbrella.server.service.DbAssetMysqlScanOfflineJobInstanceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DbAssetMysqlScanOfflineJobInstanceServiceImpl
        extends ServiceImpl<DbAssetMysqlScanOfflineJobInstanceMapper, DbAssetMysqlScanOfflineJobInstance>
        implements DbAssetMysqlScanOfflineJobInstanceService {
}
