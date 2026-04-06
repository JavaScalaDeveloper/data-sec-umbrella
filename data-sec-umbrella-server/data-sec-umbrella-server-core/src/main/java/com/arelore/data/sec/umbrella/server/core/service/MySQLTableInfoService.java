package com.arelore.data.sec.umbrella.server.core.service;

import com.arelore.data.sec.umbrella.server.core.dto.request.PageRequest;
import com.arelore.data.sec.umbrella.server.core.entity.MySQLTableInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * MySQL表信息Service接口
 */
public interface MySQLTableInfoService extends IService<MySQLTableInfo> {

    /**
     * 分页查询表信息
     */
    IPage<MySQLTableInfo> list(PageRequest request);

    /**
     * 新增表信息
     */
    Long create(MySQLTableInfo tableInfo);

    /**
     * 更新表信息
     */
    boolean update(MySQLTableInfo tableInfo);

    /**
     * 删除表信息
     */
    boolean delete(Long id);
}