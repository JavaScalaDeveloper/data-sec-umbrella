package com.arelore.data.sec.umbrella.server.core.service.impl.dataasset;

import com.arelore.data.sec.umbrella.server.core.dto.request.PageRequest;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.ClickhouseDatabaseInfo;
import com.arelore.data.sec.umbrella.server.core.enums.ManualReviewLabelEnum;
import com.arelore.data.sec.umbrella.server.core.mapper.ClickhouseDatabaseInfoMapper;
import com.arelore.data.sec.umbrella.server.core.service.ClickhouseDatabaseInfoService;
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
public class ClickhouseDatabaseInfoServiceImpl extends ServiceImpl<ClickhouseDatabaseInfoMapper, ClickhouseDatabaseInfo>
        implements ClickhouseDatabaseInfoService {

    @Override
    public IPage<ClickhouseDatabaseInfo> list(PageRequest request) {
        Long current = request.getCurrent() != null ? request.getCurrent() : 1L;
        Long size = request.getSize() != null ? request.getSize() : 10L;

        Page<ClickhouseDatabaseInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<ClickhouseDatabaseInfo> queryWrapper = new LambdaQueryWrapper<>();

        if (request.getInstance() != null && !request.getInstance().isEmpty()) {
            queryWrapper.eq(ClickhouseDatabaseInfo::getInstance, request.getInstance());
        }

        if (request.getDatabaseName() != null && !request.getDatabaseName().isEmpty()) {
            queryWrapper.like(ClickhouseDatabaseInfo::getDatabaseName, request.getDatabaseName());
        }

        if (request.getSensitivityLevelList() != null && !request.getSensitivityLevelList().isEmpty()) {
            List<String> levels = request.getSensitivityLevelList().stream()
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            if (levels.size() == 1) {
                queryWrapper.eq(ClickhouseDatabaseInfo::getSensitivityLevel, levels.get(0));
            } else if (!levels.isEmpty()) {
                queryWrapper.in(ClickhouseDatabaseInfo::getSensitivityLevel, levels);
            }
        } else if (StringUtils.hasText(request.getSensitivityLevel())) {
            queryWrapper.eq(ClickhouseDatabaseInfo::getSensitivityLevel, request.getSensitivityLevel().trim());
        }

        if (request.getSensitivityTags() != null && !request.getSensitivityTags().isEmpty()) {
            queryWrapper.like(ClickhouseDatabaseInfo::getSensitivityTags, request.getSensitivityTags());
        }

        queryWrapper.orderByDesc(ClickhouseDatabaseInfo::getCreateTime);
        return this.page(page, queryWrapper);
    }

    @Override
    public Long create(ClickhouseDatabaseInfo databaseInfo) {
        this.save(databaseInfo);
        return databaseInfo.getId();
    }

    @Override
    public boolean update(ClickhouseDatabaseInfo databaseInfo) {
        return this.updateById(databaseInfo);
    }

    @Override
    public boolean delete(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean updateManualReview(Long id, String manualReview) {
        String stored = ManualReviewLabelEnum.normalizeToStoredValue(manualReview);
        LambdaUpdateWrapper<ClickhouseDatabaseInfo> u = new LambdaUpdateWrapper<>();
        u.eq(ClickhouseDatabaseInfo::getId, id);
        u.set(ClickhouseDatabaseInfo::getManualReview, stored);
        return this.update(u);
    }
}
