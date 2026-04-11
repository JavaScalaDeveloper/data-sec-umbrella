package com.arelore.data.sec.umbrella.server.manager.controller.job;

import com.arelore.data.sec.umbrella.server.core.common.Result;
import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetMysqlScanOfflineJobIdRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.OfflineScanInstanceSnapshotDetailRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetMysqlScanOfflineJobInstanceQueryRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetMysqlScanOfflineJobQueryRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetMysqlScanOfflineJobSaveRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.DbAssetMysqlScanOfflineJobResponse;
import com.arelore.data.sec.umbrella.server.core.dto.response.OfflineScanSnapshotDetailResponse;
import com.arelore.data.sec.umbrella.server.core.dto.response.PageResponse;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.DbAssetMysqlScanOfflineJobInstance;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetMysqlScanOfflineJobInstanceService;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetMysqlScanOfflineJobService;
import com.arelore.data.sec.umbrella.server.core.manager.task.TaskManager;
import com.arelore.data.sec.umbrella.server.manager.security.AdminPermission;
import com.arelore.data.sec.umbrella.server.manager.security.PermissionAction;
import com.arelore.data.sec.umbrella.server.manager.clickhouse.OfflineScanSnapshotAssetEngineResolver;
import com.arelore.data.sec.umbrella.server.manager.clickhouse.OfflineScanSnapshotQueryService;
import com.arelore.data.sec.umbrella.server.manager.security.ProductCode;
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
@AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.READ)
public class DbAssetMysqlScanOfflineJobController {

    private final DbAssetMysqlScanOfflineJobService offlineScanJobService;
    private final DbAssetMysqlScanOfflineJobInstanceService jobInstanceService;
    private final TaskManager taskManager;
    private final OfflineScanSnapshotQueryService offlineScanSnapshotQueryService;

    @Autowired
    public DbAssetMysqlScanOfflineJobController(DbAssetMysqlScanOfflineJobService offlineScanJobService,
                                                DbAssetMysqlScanOfflineJobInstanceService jobInstanceService,
                                                TaskManager taskManager,
                                                OfflineScanSnapshotQueryService offlineScanSnapshotQueryService) {
        this.offlineScanJobService = offlineScanJobService;
        this.jobInstanceService = jobInstanceService;
        this.taskManager = taskManager;
        this.offlineScanSnapshotQueryService = offlineScanSnapshotQueryService;
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

    /**
     * 按任务实例 ID 查询 ClickHouse 中已落库的表级 / 字段级敏感快照（需 MQ 消费者写入且配置 clickhouse.*）。
     */
    @PostMapping("/instance/snapshot-detail")
    public Result<OfflineScanSnapshotDetailResponse> instanceSnapshotDetail(
            @RequestBody OfflineScanInstanceSnapshotDetailRequest request) {
        if (request.getId() == null) {
            return Result.error("id不能为空");
        }
        if (!StringUtils.hasText(request.getScanKind())) {
            return Result.error("scanKind不能为空，请指定 RULE 或 AI");
        }
        String scanKind = request.getScanKind().trim().toUpperCase();
        if (!"RULE".equals(scanKind) && !"AI".equals(scanKind)) {
            return Result.error("scanKind 仅支持 RULE 或 AI");
        }
        DbAssetMysqlScanOfflineJobInstance inst = jobInstanceService.getById(request.getId());
        if (inst == null) {
            return Result.error("实例不存在");
        }
        String engine = OfflineScanSnapshotAssetEngineResolver.resolve(inst);
        request.setEngine(engine);
        request.setScanKind(scanKind);
        try {
            return Result.success(offlineScanSnapshotQueryService.query(request));
        } catch (IllegalStateException ex) {
            return Result.error(ex.getMessage());
        }
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
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Long> create(@RequestBody DbAssetMysqlScanOfflineJobSaveRequest request) {
        try {
            Long id = offlineScanJobService.create(request);
            return Result.success(id);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/update")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
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
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
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
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
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
