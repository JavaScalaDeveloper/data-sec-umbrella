package com.arelore.data.sec.umbrella.server.service;

import com.arelore.data.sec.umbrella.server.dto.request.DbAssetMysqlScanOfflineJobIdRequest;
import com.arelore.data.sec.umbrella.server.dto.request.DbAssetMysqlScanOfflineJobQueryRequest;
import com.arelore.data.sec.umbrella.server.dto.request.DbAssetMysqlScanOfflineJobSaveRequest;
import com.arelore.data.sec.umbrella.server.dto.response.DbAssetMysqlScanOfflineJobResponse;
import com.arelore.data.sec.umbrella.server.dto.response.PageResponse;
import com.arelore.data.sec.umbrella.server.entity.DbAssetMysqlScanOfflineJob;
import com.baomidou.mybatisplus.extension.service.IService;

public interface DbAssetMysqlScanOfflineJobService extends IService<DbAssetMysqlScanOfflineJob> {

    PageResponse<DbAssetMysqlScanOfflineJobResponse> getPage(DbAssetMysqlScanOfflineJobQueryRequest request);

    DbAssetMysqlScanOfflineJobResponse getById(Long id);

    Long create(DbAssetMysqlScanOfflineJobSaveRequest request);

    boolean update(DbAssetMysqlScanOfflineJobSaveRequest request);

    boolean delete(Long id);

    /**
     * 手动触发执行（具体扫描逻辑可后续接入调度/异步任务）
     */
    boolean execute(DbAssetMysqlScanOfflineJobIdRequest request);
}
