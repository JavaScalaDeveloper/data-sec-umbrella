package com.arelore.data.sec.umbrella.server.manager.controller.dataasset;

import com.arelore.data.sec.umbrella.server.core.common.Result;
import com.arelore.data.sec.umbrella.server.core.dto.request.ManualReviewUpdateRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.PageRequest;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.ClickhouseDatabaseInfo;
import com.arelore.data.sec.umbrella.server.core.service.ClickhouseDatabaseInfoService;
import com.arelore.data.sec.umbrella.server.manager.security.AdminPermission;
import com.arelore.data.sec.umbrella.server.manager.security.PermissionAction;
import com.arelore.data.sec.umbrella.server.manager.security.ProductCode;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClickHouse 数据资产-库维度（主库表 db_asset_clickhouse_database_info）
 */
@RestController
@RequestMapping("api/data-asset/clickhouse/database")
@AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.READ)
public class ClickhouseDatabaseInfoController {

    private final ClickhouseDatabaseInfoService clickhouseDatabaseInfoService;

    @Autowired
    public ClickhouseDatabaseInfoController(ClickhouseDatabaseInfoService clickhouseDatabaseInfoService) {
        this.clickhouseDatabaseInfoService = clickhouseDatabaseInfoService;
    }

    @PostMapping("/list")
    public Result<IPage<ClickhouseDatabaseInfo>> list(@RequestBody PageRequest request) {
        return Result.success(clickhouseDatabaseInfoService.list(request));
    }

    @PostMapping("/get-by-id")
    public Result<ClickhouseDatabaseInfo> getById(@RequestBody ClickhouseDatabaseInfo request) {
        return Result.success(clickhouseDatabaseInfoService.getById(request.getId()));
    }

    @PostMapping("/create")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Long> create(@RequestBody ClickhouseDatabaseInfo databaseInfo) {
        return Result.success(clickhouseDatabaseInfoService.create(databaseInfo));
    }

    @PostMapping("/update")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> update(@RequestBody ClickhouseDatabaseInfo databaseInfo) {
        return Result.success(clickhouseDatabaseInfoService.update(databaseInfo));
    }

    @PostMapping("/update-manual-review")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> updateManualReview(@RequestBody ManualReviewUpdateRequest request) {
        try {
            return Result.success(clickhouseDatabaseInfoService.updateManualReview(request.getId(), request.getManualReview()));
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("/delete")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> delete(@RequestBody ClickhouseDatabaseInfo request) {
        return Result.success(clickhouseDatabaseInfoService.delete(request.getId()));
    }
}
