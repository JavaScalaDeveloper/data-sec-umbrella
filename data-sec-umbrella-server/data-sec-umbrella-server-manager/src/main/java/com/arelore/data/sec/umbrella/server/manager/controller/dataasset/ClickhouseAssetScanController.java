package com.arelore.data.sec.umbrella.server.manager.controller.dataasset;

import com.arelore.data.sec.umbrella.server.core.common.Result;
import com.arelore.data.sec.umbrella.server.core.schedule.ClickHouseAssetScanJob;
import com.arelore.data.sec.umbrella.server.manager.security.AdminPermission;
import com.arelore.data.sec.umbrella.server.manager.security.PermissionAction;
import com.arelore.data.sec.umbrella.server.manager.security.ProductCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/data-asset/clickhouse")
@AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.READ)
public class ClickhouseAssetScanController {

    @Autowired
    private ClickHouseAssetScanJob clickHouseAssetScanJob;

    @PostMapping("/scan")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> scan() {
        try {
            clickHouseAssetScanJob.scanDatabases();
            clickHouseAssetScanJob.scanTables();
            return Result.success(true);
        } catch (Exception e) {
            return Result.error("扫描失败：" + e.getMessage());
        }
    }

    @PostMapping("/scan-databases")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> scanDatabases() {
        try {
            clickHouseAssetScanJob.scanDatabases();
            return Result.success(true);
        } catch (Exception e) {
            return Result.error("数据库扫描失败：" + e.getMessage());
        }
    }

    @PostMapping("/scan-tables")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> scanTables() {
        try {
            clickHouseAssetScanJob.scanTables();
            return Result.success(true);
        } catch (Exception e) {
            return Result.error("表扫描失败：" + e.getMessage());
        }
    }
}
