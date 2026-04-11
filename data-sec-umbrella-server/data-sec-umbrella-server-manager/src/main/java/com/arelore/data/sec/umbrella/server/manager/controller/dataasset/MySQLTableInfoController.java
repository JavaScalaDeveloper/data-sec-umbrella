package com.arelore.data.sec.umbrella.server.manager.controller.dataasset;

import com.arelore.data.sec.umbrella.server.core.common.Result;
import com.arelore.data.sec.umbrella.server.core.dto.request.ManualReviewUpdateRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.PageRequest;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.MySQLTableInfo;
import com.arelore.data.sec.umbrella.server.core.service.MySQLTableInfoService;
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
 * MySQL表信息Controller
 */
@RestController
@RequestMapping("api/data-asset/mysql/table")
@AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.READ)
public class MySQLTableInfoController {

    private final MySQLTableInfoService mySQLTableInfoService;

    @Autowired
    public MySQLTableInfoController(MySQLTableInfoService mySQLTableInfoService) {
        this.mySQLTableInfoService = mySQLTableInfoService;
    }

    /**
     * 分页查询表信息
     */
    @PostMapping("/list")
    public Result<IPage<MySQLTableInfo>> list(@RequestBody PageRequest request) {
        IPage<MySQLTableInfo> result = mySQLTableInfoService.list(request);
        return Result.success(result);
    }

    /**
     * 根据ID查询表信息
     */
    @PostMapping("/get-by-id")
    public Result<MySQLTableInfo> getById(@RequestBody MySQLTableInfo request) {
        MySQLTableInfo result = mySQLTableInfoService.getById(request.getId());
        return Result.success(result);
    }

    /**
     * 新增表信息
     */
    @PostMapping("/create")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Long> create(@RequestBody MySQLTableInfo tableInfo) {
        Long id = mySQLTableInfoService.create(tableInfo);
        return Result.success(id);
    }

    /**
     * 更新表信息
     */
    @PostMapping("/update")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> update(@RequestBody MySQLTableInfo tableInfo) {
        boolean result = mySQLTableInfoService.update(tableInfo);
        return Result.success(result);
    }

    /**
     * 仅更新表资产人工打标（忽略/误报/敏感）；未传或空字符串表示清除打标。
     */
    @PostMapping("/update-manual-review")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> updateManualReview(@RequestBody ManualReviewUpdateRequest request) {
        try {
            boolean ok = mySQLTableInfoService.updateManualReview(request.getId(), request.getManualReview());
            return Result.success(ok);
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    /**
     * 删除表信息
     */
    @PostMapping("/delete")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> delete(@RequestBody MySQLTableInfo request) {
        boolean result = mySQLTableInfoService.delete(request.getId());
        return Result.success(result);
    }
}