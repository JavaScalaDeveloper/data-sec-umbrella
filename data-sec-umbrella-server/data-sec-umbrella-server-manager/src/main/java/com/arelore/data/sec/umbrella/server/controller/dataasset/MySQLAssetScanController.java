package com.arelore.data.sec.umbrella.server.controller.dataasset;

import com.arelore.data.sec.umbrella.server.common.Result;
import com.arelore.data.sec.umbrella.server.schedule.MySQLAssetScanJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MySQL资产扫描控制器
 */
@RestController
@RequestMapping("api/data-asset/mysql")
public class MySQLAssetScanController {

    @Autowired
    private MySQLAssetScanJob mySQLAssetScanJob;

    /**
     * 手动触发MySQL资产扫描
     */
    @PostMapping("/scan")
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
    public Result<Boolean> scanTables() {
        try {
            mySQLAssetScanJob.scanTables();
            return Result.success(true);
        } catch (Exception e) {
            return Result.error("表扫描失败：" + e.getMessage());
        }
    }
}
