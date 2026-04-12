package com.arelore.data.sec.umbrella.server.core.service;

import com.arelore.data.sec.umbrella.server.core.dto.request.PageRequest;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.ClickhouseDatabaseInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ClickhouseDatabaseInfoService extends IService<ClickhouseDatabaseInfo> {

    IPage<ClickhouseDatabaseInfo> list(PageRequest request);

    Long create(ClickhouseDatabaseInfo databaseInfo);

    boolean update(ClickhouseDatabaseInfo databaseInfo);

    boolean delete(Long id);

    boolean updateManualReview(Long id, String manualReview);
}
