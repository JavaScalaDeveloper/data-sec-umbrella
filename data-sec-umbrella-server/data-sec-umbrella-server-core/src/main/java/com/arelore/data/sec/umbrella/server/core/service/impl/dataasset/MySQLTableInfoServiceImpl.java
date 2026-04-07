package com.arelore.data.sec.umbrella.server.core.service.impl.dataasset;

import com.arelore.data.sec.umbrella.server.core.dto.request.PageRequest;
import com.arelore.data.sec.umbrella.server.core.entity.MySQLTableInfo;
import com.arelore.data.sec.umbrella.server.core.enums.ManualReviewLabelEnum;
import com.arelore.data.sec.umbrella.server.core.mapper.MySQLTableInfoMapper;
import com.arelore.data.sec.umbrella.server.core.service.MySQLTableInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * MySQL表信息Service实现类
 */
@Service
public class MySQLTableInfoServiceImpl extends ServiceImpl<MySQLTableInfoMapper, MySQLTableInfo> implements MySQLTableInfoService {

    @Override
    public IPage<MySQLTableInfo> list(PageRequest request) {
        Long current = request.getCurrent() != null ? request.getCurrent() : 1L;
        Long size = request.getSize() != null ? request.getSize() : 10L;

        Page<MySQLTableInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<MySQLTableInfo> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据实例查询
        if (request.getInstance() != null && !request.getInstance().isEmpty()) {
            queryWrapper.eq(MySQLTableInfo::getInstance, request.getInstance());
        }
        
        // 根据数据库名查询
        if (request.getDatabaseName() != null && !request.getDatabaseName().isEmpty()) {
            queryWrapper.eq(MySQLTableInfo::getDatabaseName, request.getDatabaseName());
        }
        
        // 根据表名查询
        if (request.getTableName() != null && !request.getTableName().isEmpty()) {
            queryWrapper.like(MySQLTableInfo::getTableName, request.getTableName());
        }
        
        // 根据敏感等级查询
        if (request.getSensitivityLevel() != null && !request.getSensitivityLevel().isEmpty()) {
            queryWrapper.eq(MySQLTableInfo::getSensitivityLevel, request.getSensitivityLevel());
        }
        
        // 根据敏感标签查询
        if (request.getSensitivityTags() != null && !request.getSensitivityTags().isEmpty()) {
            queryWrapper.like(MySQLTableInfo::getSensitivityTags, request.getSensitivityTags());
        }
        
        queryWrapper.orderByDesc(MySQLTableInfo::getCreateTime);
        return this.page(page, queryWrapper);
    }

    @Override
    public Long create(MySQLTableInfo tableInfo) {
        this.save(tableInfo);
        return tableInfo.getId();
    }

    @Override
    public boolean update(MySQLTableInfo tableInfo) {
        return this.updateById(tableInfo);
    }

    @Override
    public boolean delete(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean updateManualReview(Long id, String manualReview) {
        String stored = ManualReviewLabelEnum.normalizeToStoredValue(manualReview);
        LambdaUpdateWrapper<MySQLTableInfo> u = new LambdaUpdateWrapper<>();
        u.eq(MySQLTableInfo::getId, id);
        u.set(MySQLTableInfo::getManualReview, stored);
        return this.update(u);
    }
}