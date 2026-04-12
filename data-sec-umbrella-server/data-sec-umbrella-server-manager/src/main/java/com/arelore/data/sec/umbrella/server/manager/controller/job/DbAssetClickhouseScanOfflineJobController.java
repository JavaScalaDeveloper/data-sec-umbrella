package com.arelore.data.sec.umbrella.server.manager.controller.job;

import com.arelore.data.sec.umbrella.server.core.common.Result;
import com.arelore.data.sec.umbrella.server.core.constant.OfflineScanJobDatabaseType;
import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetMysqlScanOfflineJobIdRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetMysqlScanOfflineJobInstanceQueryRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetMysqlScanOfflineJobQueryRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.DbAssetMysqlScanOfflineJobSaveRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.OfflineScanInstanceSnapshotDetailRequest;
import com.arelore.data.sec.umbrella.server.core.dto.response.DbAssetMysqlScanOfflineJobResponse;
import com.arelore.data.sec.umbrella.server.core.dto.response.OfflineScanSnapshotDetailResponse;
import com.arelore.data.sec.umbrella.server.core.dto.response.PageResponse;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.DbAssetMysqlScanOfflineJobInstance;
import com.arelore.data.sec.umbrella.server.core.enums.OfflineJobRunStatusEnum;
import com.arelore.data.sec.umbrella.server.core.manager.task.TaskManager;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetMysqlScanOfflineJobInstanceService;
import com.arelore.data.sec.umbrella.server.core.service.DbAssetMysqlScanOfflineJobService;
import com.arelore.data.sec.umbrella.server.manager.clickhouse.OfflineScanSnapshotAssetEngineResolver;
import com.arelore.data.sec.umbrella.server.manager.clickhouse.OfflineScanSnapshotQueryService;
import com.arelore.data.sec.umbrella.server.manager.security.AdminPermission;
import com.arelore.data.sec.umbrella.server.manager.security.PermissionAction;
import com.arelore.data.sec.umbrella.server.manager.security.ProductCode;
import com.arelore.data.sec.umbrella.server.manager.task.infra.RedisTaskCacheUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClickHouse 离线批量扫描任务（仅 Clickhouse 引擎任务与实例）
 */
@RestController
@RequestMapping("api/db-asset/clickhouse/offline-scan-job")
@AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.READ)
public class DbAssetClickhouseScanOfflineJobController {

    private final DbAssetMysqlScanOfflineJobService offlineScanJobService;
    private final DbAssetMysqlScanOfflineJobInstanceService jobInstanceService;
    private final TaskManager taskManager;
    private final OfflineScanSnapshotQueryService offlineScanSnapshotQueryService;
    private final RedisTaskCacheUtil redisTaskCacheUtil;

    @Autowired
    public DbAssetClickhouseScanOfflineJobController(DbAssetMysqlScanOfflineJobService offlineScanJobService,
                                                     DbAssetMysqlScanOfflineJobInstanceService jobInstanceService,
                                                     TaskManager taskManager,
                                                     OfflineScanSnapshotQueryService offlineScanSnapshotQueryService,
                                                     RedisTaskCacheUtil redisTaskCacheUtil) {
        this.offlineScanJobService = offlineScanJobService;
        this.jobInstanceService = jobInstanceService;
        this.taskManager = taskManager;
        this.offlineScanSnapshotQueryService = offlineScanSnapshotQueryService;
        this.redisTaskCacheUtil = redisTaskCacheUtil;
    }

    @PostMapping("/list")
    public Result<PageResponse<DbAssetMysqlScanOfflineJobResponse>> list(@RequestBody DbAssetMysqlScanOfflineJobQueryRequest request) {
        request.setDatabaseType(OfflineScanJobDatabaseType.CLICKHOUSE);
        return Result.success(offlineScanJobService.getPage(request));
    }

    @PostMapping("/instance/list")
    public Result<PageResponse<DbAssetMysqlScanOfflineJobInstance>> listInstances(@RequestBody DbAssetMysqlScanOfflineJobInstanceQueryRequest request) {
        request.setDatabaseType(OfflineScanJobDatabaseType.CLICKHOUSE);
        IPage<DbAssetMysqlScanOfflineJobInstance> pageResult = jobInstanceService.pageQuery(request);
        return Result.success(new PageResponse<>(
                pageResult.getRecords(),
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize()
        ));
    }

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
        if (!OfflineScanJobDatabaseType.CLICKHOUSE.equals(OfflineScanJobDatabaseType.normalizeInstance(inst.getDatabaseType()))) {
            return Result.error("实例不属于 ClickHouse 引擎");
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
        if (data == null || !OfflineScanJobDatabaseType.CLICKHOUSE.equals(OfflineScanJobDatabaseType.normalizeJob(data.getDatabaseType()))) {
            return Result.error("任务不存在");
        }
        return Result.success(data);
    }

    @PostMapping("/create")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Long> create(@RequestBody DbAssetMysqlScanOfflineJobSaveRequest request) {
        try {
            request.setDatabaseType(OfflineScanJobDatabaseType.CLICKHOUSE);
            return Result.success(offlineScanJobService.create(request));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/update")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> update(@RequestBody DbAssetMysqlScanOfflineJobSaveRequest request) {
        try {
            request.setDatabaseType(OfflineScanJobDatabaseType.CLICKHOUSE);
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
        DbAssetMysqlScanOfflineJobResponse data = offlineScanJobService.getById(request.getId());
        if (data == null || !OfflineScanJobDatabaseType.CLICKHOUSE.equals(OfflineScanJobDatabaseType.normalizeJob(data.getDatabaseType()))) {
            return Result.error("任务不存在");
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
            Long instanceId = offlineScanJobService.execute(request, OfflineScanJobDatabaseType.CLICKHOUSE);
            if (instanceId == null) {
                return Result.error("任务不存在");
            }
            taskManager.dispatchOfflineMysqlScan();
            return Result.success(instanceId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/instance/stop")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> stopInstance(@RequestBody DbAssetMysqlScanOfflineJobIdRequest request) {
        if (request.getId() == null) {
            return Result.error("id不能为空");
        }
        DbAssetMysqlScanOfflineJobInstance inst = jobInstanceService.getById(request.getId());
        if (inst == null) {
            return Result.error("实例不存在");
        }
        if (!OfflineScanJobDatabaseType.CLICKHOUSE.equals(OfflineScanJobDatabaseType.normalizeInstance(inst.getDatabaseType()))) {
            return Result.error("实例不属于 ClickHouse 引擎");
        }
        String rs = inst.getRunStatus();
        if (!OfflineJobRunStatusEnum.WAITING.getValue().equals(rs)
                && !OfflineJobRunStatusEnum.RUNNING.getValue().equals(rs)) {
            return Result.error("仅等待中或运行中的实例可停止");
        }
        String previous = rs;
        inst.setRunStatus(OfflineJobRunStatusEnum.STOPPED.getValue());
        if (!jobInstanceService.updateById(inst)) {
            return Result.error("更新实例状态失败");
        }
        try {
            redisTaskCacheUtil.bumpDispatchVersion(request.getId());
        } catch (Exception ex) {
            inst.setRunStatus(previous);
            jobInstanceService.updateById(inst);
            return Result.error("停止失败（Redis版本更新异常）: " + ex.getMessage());
        }
        return Result.success(true);
    }
}
