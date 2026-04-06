package com.arelore.data.sec.umbrella.server.controller.dataasset;

import com.arelore.data.sec.umbrella.server.common.Result;
import com.arelore.data.sec.umbrella.server.dto.request.PageRequest;
import com.arelore.data.sec.umbrella.server.entity.MySQLDatabaseInfo;
import com.arelore.data.sec.umbrella.server.service.MySQLDatabaseInfoService;
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
    public Result<Long> create(@RequestBody MySQLDatabaseInfo databaseInfo) {
        Long id = mySQLDatabaseInfoService.create(databaseInfo);
        return Result.success(id);
    }

    /**
     * 更新数据库信息
     */
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody MySQLDatabaseInfo databaseInfo) {
        boolean result = mySQLDatabaseInfoService.update(databaseInfo);
        return Result.success(result);
    }

    /**
     * 删除数据库信息
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody MySQLDatabaseInfo request) {
        boolean result = mySQLDatabaseInfoService.delete(request.getId());
        return Result.success(result);
    }
}