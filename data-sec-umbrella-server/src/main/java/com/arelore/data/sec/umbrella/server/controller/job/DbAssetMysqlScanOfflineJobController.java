package com.arelore.data.sec.umbrella.server.controller.job;

import com.arelore.data.sec.umbrella.server.common.Result;
import com.arelore.data.sec.umbrella.server.dto.request.DbAssetMysqlScanOfflineJobIdRequest;
import com.arelore.data.sec.umbrella.server.dto.request.DbAssetMysqlScanOfflineJobQueryRequest;
import com.arelore.data.sec.umbrella.server.dto.request.DbAssetMysqlScanOfflineJobSaveRequest;
import com.arelore.data.sec.umbrella.server.dto.response.DbAssetMysqlScanOfflineJobResponse;
import com.arelore.data.sec.umbrella.server.dto.response.PageResponse;
import com.arelore.data.sec.umbrella.server.service.DbAssetMysqlScanOfflineJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MySQL 数据资产离线扫描任务
 */
@RestController
@RequestMapping("api/db-asset/mysql/offline-scan-job")
public class DbAssetMysqlScanOfflineJobController {

    private final DbAssetMysqlScanOfflineJobService offlineScanJobService;

    @Autowired
    public DbAssetMysqlScanOfflineJobController(DbAssetMysqlScanOfflineJobService offlineScanJobService) {
        this.offlineScanJobService = offlineScanJobService;
    }

    @PostMapping("/list")
    public Result<PageResponse<DbAssetMysqlScanOfflineJobResponse>> list(@RequestBody DbAssetMysqlScanOfflineJobQueryRequest request) {
        return Result.success(offlineScanJobService.getPage(request));
    }

    @PostMapping("/getById")
    public Result<DbAssetMysqlScanOfflineJobResponse> getById(@RequestBody DbAssetMysqlScanOfflineJobIdRequest request) {
        if (request.getId() == null) {
            return Result.error("id不能为空");
        }
        DbAssetMysqlScanOfflineJobResponse data = offlineScanJobService.getById(request.getId());
        if (data == null) {
            return Result.error("任务不存在");
        }
        return Result.success(data);
    }

    @PostMapping("/create")
    public Result<Long> create(@RequestBody DbAssetMysqlScanOfflineJobSaveRequest request) {
        try {
            Long id = offlineScanJobService.create(request);
            return Result.success(id);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody DbAssetMysqlScanOfflineJobSaveRequest request) {
        try {
            boolean ok = offlineScanJobService.update(request);
            if (!ok) {
                return Result.error("更新失败，任务不存在");
            }
            return Result.success(true);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody DbAssetMysqlScanOfflineJobIdRequest request) {
        if (request.getId() == null) {
            return Result.error("id不能为空");
        }
        boolean ok = offlineScanJobService.delete(request.getId());
        if (!ok) {
            return Result.error("删除失败");
        }
        return Result.success(true);
    }

    @PostMapping("/execute")
    public Result<Boolean> execute(@RequestBody DbAssetMysqlScanOfflineJobIdRequest request) {
        try {
            boolean ok = offlineScanJobService.execute(request);
            if (!ok) {
                return Result.error("任务不存在");
            }
            return Result.success(true);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }
}
