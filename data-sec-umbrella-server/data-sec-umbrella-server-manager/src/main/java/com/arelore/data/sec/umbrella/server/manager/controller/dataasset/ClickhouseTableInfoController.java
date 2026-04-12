package com.arelore.data.sec.umbrella.server.manager.controller.dataasset;

import com.arelore.data.sec.umbrella.server.core.common.Result;
import com.arelore.data.sec.umbrella.server.core.dto.request.ManualReviewUpdateRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.PageRequest;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.ClickhouseTableInfo;
import com.arelore.data.sec.umbrella.server.core.service.ClickhouseTableInfoService;
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
 * ClickHouse 数据资产-表维度（主库表 db_asset_clickhouse_table_info）
 */
@RestController
@RequestMapping("api/data-asset/clickhouse/table")
@AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.READ)
public class ClickhouseTableInfoController {

    private final ClickhouseTableInfoService clickhouseTableInfoService;

    @Autowired
    public ClickhouseTableInfoController(ClickhouseTableInfoService clickhouseTableInfoService) {
        this.clickhouseTableInfoService = clickhouseTableInfoService;
    }

    @PostMapping("/list")
    public Result<IPage<ClickhouseTableInfo>> list(@RequestBody PageRequest request) {
        return Result.success(clickhouseTableInfoService.list(request));
    }

    @PostMapping("/get-by-id")
    public Result<ClickhouseTableInfo> getById(@RequestBody ClickhouseTableInfo request) {
        return Result.success(clickhouseTableInfoService.getById(request.getId()));
    }

    @PostMapping("/create")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Long> create(@RequestBody ClickhouseTableInfo tableInfo) {
        return Result.success(clickhouseTableInfoService.create(tableInfo));
    }

    @PostMapping("/update")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> update(@RequestBody ClickhouseTableInfo tableInfo) {
        return Result.success(clickhouseTableInfoService.update(tableInfo));
    }

    @PostMapping("/update-manual-review")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> updateManualReview(@RequestBody ManualReviewUpdateRequest request) {
        try {
            return Result.success(clickhouseTableInfoService.updateManualReview(request.getId(), request.getManualReview()));
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("/delete")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> delete(@RequestBody ClickhouseTableInfo request) {
        return Result.success(clickhouseTableInfoService.delete(request.getId()));
    }
}
