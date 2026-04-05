package com.arelore.data.sec.umbrella.server.service;

import com.arelore.data.sec.umbrella.server.dto.request.PageRequest;
import com.arelore.data.sec.umbrella.server.entity.MySQLDatabaseInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * MySQL数据库信息Service接口
 */
public interface MySQLDatabaseInfoService extends IService<MySQLDatabaseInfo> {

    /**
     * 分页查询数据库信息
     */
    IPage<MySQLDatabaseInfo> list(PageRequest request);

    /**
     * 新增数据库信息
     */
    Long create(MySQLDatabaseInfo databaseInfo);

    /**
     * 更新数据库信息
     */
    boolean update(MySQLDatabaseInfo databaseInfo);

    /**
     * 删除数据库信息
     */
    boolean delete(Long id);
}