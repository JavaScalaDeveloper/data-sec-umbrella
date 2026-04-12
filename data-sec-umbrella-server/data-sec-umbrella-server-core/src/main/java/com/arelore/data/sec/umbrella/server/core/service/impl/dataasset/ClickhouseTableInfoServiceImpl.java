package com.arelore.data.sec.umbrella.server.core.service.impl.dataasset;

import com.arelore.data.sec.umbrella.server.core.dto.request.PageRequest;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.ClickhouseTableInfo;
import com.arelore.data.sec.umbrella.server.core.enums.ManualReviewLabelEnum;
import com.arelore.data.sec.umbrella.server.core.mapper.ClickhouseTableInfoMapper;
import com.arelore.data.sec.umbrella.server.core.service.ClickhouseTableInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClickhouseTableInfoServiceImpl extends ServiceImpl<ClickhouseTableInfoMapper, ClickhouseTableInfo>
        implements ClickhouseTableInfoService {

    @Override
    public IPage<ClickhouseTableInfo> list(PageRequest request) {
        Long current = request.getCurrent() != null ? request.getCurrent() : 1L;
        Long size = request.getSize() != null ? request.getSize() : 10L;

        Page<ClickhouseTableInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<ClickhouseTableInfo> queryWrapper = new LambdaQueryWrapper<>();

        if (request.getInstance() != null && !request.getInstance().isEmpty()) {
            queryWrapper.eq(ClickhouseTableInfo::getInstance, request.getInstance());
        }

        if (request.getDatabaseName() != null && !request.getDatabaseName().isEmpty()) {
            queryWrapper.eq(ClickhouseTableInfo::getDatabaseName, request.getDatabaseName());
        }

        if (request.getTableName() != null && !request.getTableName().isEmpty()) {
            queryWrapper.like(ClickhouseTableInfo::getTableName, request.getTableName());
        }

        if (request.getSensitivityLevelList() != null && !request.getSensitivityLevelList().isEmpty()) {
            List<String> levels = request.getSensitivityLevelList().stream()
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            if (levels.size() == 1) {
                queryWrapper.eq(ClickhouseTableInfo::getSensitivityLevel, levels.get(0));
            } else if (!levels.isEmpty()) {
                queryWrapper.in(ClickhouseTableInfo::getSensitivityLevel, levels);
            }
        } else if (StringUtils.hasText(request.getSensitivityLevel())) {
            queryWrapper.eq(ClickhouseTableInfo::getSensitivityLevel, request.getSensitivityLevel().trim());
        }

        if (request.getSensitivityTags() != null && !request.getSensitivityTags().isEmpty()) {
            queryWrapper.like(ClickhouseTableInfo::getSensitivityTags, request.getSensitivityTags());
        }

        queryWrapper.orderByDesc(ClickhouseTableInfo::getCreateTime);
        return this.page(page, queryWrapper);
    }

    @Override
    public Long create(ClickhouseTableInfo tableInfo) {
        this.save(tableInfo);
        return tableInfo.getId();
    }

    @Override
    public boolean update(ClickhouseTableInfo tableInfo) {
        return this.updateById(tableInfo);
    }

    @Override
    public boolean delete(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean updateManualReview(Long id, String manualReview) {
        String stored = ManualReviewLabelEnum.normalizeToStoredValue(manualReview);
        LambdaUpdateWrapper<ClickhouseTableInfo> u = new LambdaUpdateWrapper<>();
        u.eq(ClickhouseTableInfo::getId, id);
        u.set(ClickhouseTableInfo::getManualReview, stored);
        return this.update(u);
    }
}
