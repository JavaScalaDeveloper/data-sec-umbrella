package com.arelore.data.sec.umbrella.server.core.service.impl.dataasset;

import com.arelore.data.sec.umbrella.server.core.dto.request.PageRequest;
import com.arelore.data.sec.umbrella.server.core.entity.MySQLDatabaseInfo;
import com.arelore.data.sec.umbrella.server.core.enums.ManualReviewLabelEnum;
import com.arelore.data.sec.umbrella.server.core.mapper.MySQLDatabaseInfoMapper;
import com.arelore.data.sec.umbrella.server.core.service.MySQLDatabaseInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * MySQL数据库信息Service实现类
 */
@Service
public class MySQLDatabaseInfoServiceImpl extends ServiceImpl<MySQLDatabaseInfoMapper, MySQLDatabaseInfo> implements MySQLDatabaseInfoService {

    @Override
    public IPage<MySQLDatabaseInfo> list(PageRequest request) {
        Long current = request.getCurrent() != null ? request.getCurrent() : 1L;
        Long size = request.getSize() != null ? request.getSize() : 10L;

        Page<MySQLDatabaseInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<MySQLDatabaseInfo> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据实例查询
        if (request.getInstance() != null && !request.getInstance().isEmpty()) {
            queryWrapper.eq(MySQLDatabaseInfo::getInstance, request.getInstance());
        }
        
        // 根据数据库名查询
        if (request.getDatabaseName() != null && !request.getDatabaseName().isEmpty()) {
            queryWrapper.like(MySQLDatabaseInfo::getDatabaseName, request.getDatabaseName());
        }
        
        // 根据敏感等级查询
        if (request.getSensitivityLevel() != null && !request.getSensitivityLevel().isEmpty()) {
            queryWrapper.eq(MySQLDatabaseInfo::getSensitivityLevel, request.getSensitivityLevel());
        }
        
        // 根据敏感标签查询
        if (request.getSensitivityTags() != null && !request.getSensitivityTags().isEmpty()) {
            queryWrapper.like(MySQLDatabaseInfo::getSensitivityTags, request.getSensitivityTags());
        }
        
        queryWrapper.orderByDesc(MySQLDatabaseInfo::getCreateTime);
        return this.page(page, queryWrapper);
    }

    @Override
    public Long create(MySQLDatabaseInfo databaseInfo) {
        this.save(databaseInfo);
        return databaseInfo.getId();
    }

    @Override
    public boolean update(MySQLDatabaseInfo databaseInfo) {
        return this.updateById(databaseInfo);
    }

    @Override
    public boolean delete(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean updateManualReview(Long id, String manualReview) {
        String stored = ManualReviewLabelEnum.normalizeToStoredValue(manualReview);
        LambdaUpdateWrapper<MySQLDatabaseInfo> u = new LambdaUpdateWrapper<>();
        u.eq(MySQLDatabaseInfo::getId, id);
        u.set(MySQLDatabaseInfo::getManualReview, stored);
        return this.update(u);
    }
}