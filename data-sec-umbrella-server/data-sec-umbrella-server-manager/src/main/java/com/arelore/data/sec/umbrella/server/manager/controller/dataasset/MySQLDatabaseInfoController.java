package com.arelore.data.sec.umbrella.server.manager.controller.dataasset;

import com.arelore.data.sec.umbrella.server.core.common.Result;
import com.arelore.data.sec.umbrella.server.core.dto.request.ManualReviewUpdateRequest;
import com.arelore.data.sec.umbrella.server.core.dto.request.PageRequest;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.MySQLDatabaseInfo;
import com.arelore.data.sec.umbrella.server.core.service.MySQLDatabaseInfoService;
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
 * MySQL数据库信息Controller
 */
@RestController
@RequestMapping("api/data-asset/mysql/database")
@AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.READ)
public class MySQLDatabaseInfoController {

    private final MySQLDatabaseInfoService mySQLDatabaseInfoService;

    @Autowired
    public MySQLDatabaseInfoController(MySQLDatabaseInfoService mySQLDatabaseInfoService) {
        this.mySQLDatabaseInfoService = mySQLDatabaseInfoService;
    }

    /**
     * 分页查询数据库信息
     */
    @PostMapping("/list")
    public Result<IPage<MySQLDatabaseInfo>> list(@RequestBody PageRequest request) {
        IPage<MySQLDatabaseInfo> result = mySQLDatabaseInfoService.list(request);
        return Result.success(result);
    }

    /**
     * 根据ID查询数据库信息
     */
    @PostMapping("/get-by-id")
    public Result<MySQLDatabaseInfo> getById(@RequestBody MySQLDatabaseInfo request) {
        MySQLDatabaseInfo result = mySQLDatabaseInfoService.getById(request.getId());
        return Result.success(result);
    }

    /**
     * 新增数据库信息
     */
    @PostMapping("/create")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Long> create(@RequestBody MySQLDatabaseInfo databaseInfo) {
        Long id = mySQLDatabaseInfoService.create(databaseInfo);
        return Result.success(id);
    }

    /**
     * 更新数据库信息
     */
    @PostMapping("/update")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> update(@RequestBody MySQLDatabaseInfo databaseInfo) {
        boolean result = mySQLDatabaseInfoService.update(databaseInfo);
        return Result.success(result);
    }

    /**
     * 仅更新数据库资产人工打标（忽略/误报/敏感）；未传或空字符串表示清除打标。
     */
    @PostMapping("/update-manual-review")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> updateManualReview(@RequestBody ManualReviewUpdateRequest request) {
        try {
            boolean ok = mySQLDatabaseInfoService.updateManualReview(request.getId(), request.getManualReview());
            return Result.success(ok);
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    /**
     * 删除数据库信息
     */
    @PostMapping("/delete")
    @AdminPermission(product = ProductCode.DATABASE, action = PermissionAction.WRITE)
    public Result<Boolean> delete(@RequestBody MySQLDatabaseInfo request) {
        boolean result = mySQLDatabaseInfoService.delete(request.getId());
        return Result.success(result);
    }
}