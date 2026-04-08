package com.arelore.data.sec.umbrella.server.manager.controller.dataasset;

import com.arelore.data.sec.umbrella.server.core.common.Result;
import com.arelore.data.sec.umbrella.server.core.schedule.MySQLAssetScanJob;
import com.arelore.data.sec.umbrella.server.manager.security.AdminPermission;
import com.arelore.data.sec.umbrella.server.manager.security.PermissionAction;
import com.arelore.data.sec.umbrella.server.manager.security.ProductCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MySQL资产扫描控制器
 */
@RestController
@RequestMapping("api/data-asset/mysql")
@AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.READ)
public class MySQLAssetScanController {

    @Autowired
    private MySQLAssetScanJob mySQLAssetScanJob;

    /**
     * 手动触发MySQL资产扫描
     */
    @PostMapping("/scan")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> scanMySQLAssets() {
        try {
            // 扫描数据库
            mySQLAssetScanJob.scanDatabases();
            // 扫描表
            mySQLAssetScanJob.scanTables();
            return Result.success(true);
        } catch (Exception e) {
            return Result.error("扫描失败：" + e.getMessage());
        }
    }

    /**
     * 手动触发数据库扫描
     */
    @PostMapping("/scan-databases")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> scanDatabases() {
        try {
            mySQLAssetScanJob.scanDatabases();
            return Result.success(true);
        } catch (Exception e) {
            return Result.error("数据库扫描失败：" + e.getMessage());
        }
    }

    /**
     * 手动触发表扫描
     */
    @PostMapping("/scan-tables")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> scanTables() {
        try {
            mySQLAssetScanJob.scanTables();
            return Result.success(true);
        } catch (Exception e) {
            return Result.error("表扫描失败：" + e.getMessage());
        }
    }
}
