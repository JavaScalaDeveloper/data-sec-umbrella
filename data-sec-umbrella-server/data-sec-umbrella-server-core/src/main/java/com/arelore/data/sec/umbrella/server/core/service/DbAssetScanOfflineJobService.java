package com.arelore.data.sec.umbrella.server.core.service;

import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetScanOfflineJobIdRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetScanOfflineJobQueryRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetScanOfflineJobSaveRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.DbAssetScanOfflineJobResponse;
import com.arelore.data.sec.umbrella.server.core.dto.response.PageResponse;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.DbAssetScanOfflineJob;
import com.arelore.data.sec.umbrella.server.core.manager.task.TaskManager;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 离线扫描任务配置服务接口。
 *
 * @author 黄佳豪
 */
public interface DbAssetScanOfflineJobService extends IService<DbAssetScanOfflineJob> {

    /**
     * 分页查询任务配置。
     *
     * @param request 分页查询参数
     * @return 分页结果
     */
    PageResponse<DbAssetScanOfflineJobResponse> getPage(DbAssetScanOfflineJobQueryRequest request);

    /**
     * 根据 ID 查询任务配置。
     *
     * @param id 任务 ID
     * @return 任务配置
     */
    DbAssetScanOfflineJobResponse getById(Long id);

    /**
     * 新建任务配置。
     *
     * @param request 创建参数
     * @return 新建任务 ID
     */
    Long create(DbAssetScanOfflineJobSaveRequest request);

    /**
     * 更新任务配置。
     *
     * @param request 更新参数
     * @return 是否更新成功
     */
    boolean update(DbAssetScanOfflineJobSaveRequest request);

    /**
     * 删除任务配置。
     *
     * @param id 任务 ID
     * @return 是否删除成功
     */
    boolean delete(Long id);

    /**
     * 手动触发执行：创建一条「等待运行」的实例并返回实例 ID；具体分发由 {@link TaskManager} 完成。
     */
    Long execute(DbAssetScanOfflineJobIdRequest request, String apiDatabaseType);

    /**
     * 通过任务名与引擎获取最新任务配置（按 id 倒序）。
     *
     * @param taskName     任务名
     * @param databaseType MySQL / Clickhouse（{@link com.arelore.data.sec.umbrella.server.core.constant.OfflineScanJobDatabaseType}）
     */
    DbAssetScanOfflineJob findLatestByTaskName(String taskName, String databaseType);
}
