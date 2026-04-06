package com.arelore.data.sec.umbrella.server.manager.controller.job;

import com.arelore.data.sec.umbrella.server.core.common.Result;
import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetMysqlScanOfflineJobIdRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetMysqlScanOfflineJobInstanceQueryRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetMysqlScanOfflineJobQueryRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetMysqlScanOfflineJobSaveRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.DbAssetMysqlScanOfflineJobResponse;
import com.arelore.data.sec.umbrella.server.core.dto.response.PageResponse;
import com.arelore.data.sec.umbrella.server.core.entity.DbAssetMysqlScanOfflineJobInstance;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetMysqlScanOfflineJobInstanceService;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetMysqlScanOfflineJobService;
import com.arelore.data.sec.umbrella.server.core.manager.task.TaskManager;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
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
    private final DbAssetMysqlScanOfflineJobInstanceService jobInstanceService;
    private final TaskManager taskManager;

    @Autowired
    public DbAssetMysqlScanOfflineJobController(DbAssetMysqlScanOfflineJobService offlineScanJobService,
                                                DbAssetMysqlScanOfflineJobInstanceService jobInstanceService,
                                                TaskManager taskManager) {
        this.offlineScanJobService = offlineScanJobService;
        this.jobInstanceService = jobInstanceService;
        this.taskManager = taskManager;
    }

    @PostMapping("/list")
    public Result<PageResponse<DbAssetMysqlScanOfflineJobResponse>> list(@RequestBody DbAssetMysqlScanOfflineJobQueryRequest request) {
        return Result.success(offlineScanJobService.getPage(request));
    }

    @PostMapping("/instance/list")
    public Result<PageResponse<DbAssetMysqlScanOfflineJobInstance>> listInstances(@RequestBody DbAssetMysqlScanOfflineJobInstanceQueryRequest request) {
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
        w.orderByDesc(DbAssetMysqlScanOfflineJobInstance::getId);

        IPage<DbAssetMysqlScanOfflineJobInstance> pageResult = jobInstanceService.page(page, w);
        return Result.success(new PageResponse<>(
                pageResult.getRecords(),
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize()
        ));
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
    public Result<Long> execute(@RequestBody DbAssetMysqlScanOfflineJobIdRequest request) {
        try {
            Long instanceId = offlineScanJobService.execute(request);
            if (instanceId == null) {
                return Result.error("任务不存在");
            }
            taskManager.dispatchOfflineMysqlScan();
            return Result.success(instanceId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }
}
