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
     * 手动触发执行：创建一条「等待运行」的实例并返回实例 ID；具体分发由 {@link com.arelore.data.sec.umbrella.server.task.TaskManager} 完成。
     */
    Long execute(DbAssetMysqlScanOfflineJobIdRequest request);
}
