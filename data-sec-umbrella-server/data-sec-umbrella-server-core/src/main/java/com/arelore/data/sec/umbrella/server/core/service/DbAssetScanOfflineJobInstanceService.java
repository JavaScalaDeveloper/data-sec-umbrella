package com.arelore.data.sec.umbrella.server.core.service;

import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetScanOfflineJobInstanceQueryRequest;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.DbAssetScanOfflineJobInstance;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 离线扫描任务实例服务接口。
 *
 * @author 黄佳豪
 */
public interface DbAssetScanOfflineJobInstanceService extends IService<DbAssetScanOfflineJobInstance> {
    /**
     * 查询所有处于 waiting 状态的任务实例（按 id 升序）。
     *
     * @return 等待中的任务实例列表
     */
    List<DbAssetScanOfflineJobInstance> listWaitingInstances();

    /**
     * 任务实例分页（含按 {@link com.arelore.data.sec.umbrella.server.core.constant.OfflineScanJobDatabaseType} 过滤）。
     */
    IPage<DbAssetScanOfflineJobInstance> pageQuery(DbAssetScanOfflineJobInstanceQueryRequest request);
}
