package com.arelore.data.sec.umbrella.server.core.service;

import com.arelore.data.sec.umbrella.server.core.dto.request.PageRequest;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.ClickhouseTableInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ClickhouseTableInfoService extends IService<ClickhouseTableInfo> {

    IPage<ClickhouseTableInfo> list(PageRequest request);

    Long create(ClickhouseTableInfo tableInfo);

    boolean update(ClickhouseTableInfo tableInfo);

    boolean delete(Long id);

    boolean updateManualReview(Long id, String manualReview);
}
