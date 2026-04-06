package com.arelore.data.sec.umbrella.server.service.impl.dataasset;

import com.arelore.data.sec.umbrella.server.dto.request.DbAssetMysqlScanOfflineJobIdRequest;
import com.arelore.data.sec.umbrella.server.dto.request.DbAssetMysqlScanOfflineJobQueryRequest;
import com.arelore.data.sec.umbrella.server.dto.request.DbAssetMysqlScanOfflineJobSaveRequest;
import com.arelore.data.sec.umbrella.server.dto.response.DbAssetMysqlScanOfflineJobResponse;
import com.arelore.data.sec.umbrella.server.dto.response.PageResponse;
import com.arelore.data.sec.umbrella.server.entity.DbAssetMysqlScanOfflineJob;
import com.arelore.data.sec.umbrella.server.entity.DbAssetMysqlScanOfflineJobInstance;
import com.arelore.data.sec.umbrella.server.mapper.DbAssetMysqlScanOfflineJobMapper;
import com.arelore.data.sec.umbrella.server.service.DbAssetMysqlScanOfflineJobInstanceService;
import com.arelore.data.sec.umbrella.server.service.DbAssetMysqlScanOfflineJobService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DbAssetMysqlScanOfflineJobServiceImpl extends ServiceImpl<DbAssetMysqlScanOfflineJobMapper, DbAssetMysqlScanOfflineJob>
        implements DbAssetMysqlScanOfflineJobService {

    private static final int SAMPLE_MIN = 1;
    private static final int SAMPLE_MAX = 200;

    private final DbAssetMysqlScanOfflineJobInstanceService jobInstanceService;

    public DbAssetMysqlScanOfflineJobServiceImpl(DbAssetMysqlScanOfflineJobInstanceService jobInstanceService) {
        this.jobInstanceService = jobInstanceService;
    }

    @Override
    public PageResponse<DbAssetMysqlScanOfflineJobResponse> getPage(DbAssetMysqlScanOfflineJobQueryRequest request) {
        LambdaQueryWrapper<DbAssetMysqlScanOfflineJob> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getTaskName())) {
            w.like(DbAssetMysqlScanOfflineJob::getTaskName, request.getTaskName().trim());
        }
        w.orderByDesc(DbAssetMysqlScanOfflineJob::getId);
        long current = request.getCurrent() != null ? request.getCurrent() : 1L;
        long size = request.getSize() != null ? request.getSize() : 10L;
        Page<DbAssetMysqlScanOfflineJob> page = new Page<>(current, size);
        IPage<DbAssetMysqlScanOfflineJob> pageResult = this.page(page, w);
        List<DbAssetMysqlScanOfflineJobResponse> records = pageResult.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new PageResponse<>(records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public DbAssetMysqlScanOfflineJobResponse getById(Long id) {
        DbAssetMysqlScanOfflineJob entity = super.getById(id);
        return entity == null ? null : toResponse(entity);
    }

    @Override
    public Long create(DbAssetMysqlScanOfflineJobSaveRequest request) {
        validateSave(request, true);
        DbAssetMysqlScanOfflineJob entity = new DbAssetMysqlScanOfflineJob();
        BeanUtils.copyProperties(request, entity);
        entity.setId(null);
        applyDefaults(entity);
        if (!StringUtils.hasText(entity.getCreator())) {
            entity.setCreator("");
        }
        entity.setModifier("");
        this.save(entity);
        return entity.getId();
    }

    @Override
    public boolean update(DbAssetMysqlScanOfflineJobSaveRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        validateSave(request, false);
        DbAssetMysqlScanOfflineJob existing = super.getById(request.getId());
        if (existing == null) {
            return false;
        }
        DbAssetMysqlScanOfflineJob entity = new DbAssetMysqlScanOfflineJob();
        BeanUtils.copyProperties(request, entity);
        applyDefaults(entity);
        if (!StringUtils.hasText(entity.getModifier())) {
            entity.setModifier("");
        }
        return this.updateById(entity);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        return this.removeById(id);
    }

    @Override
    public Long execute(DbAssetMysqlScanOfflineJobIdRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        DbAssetMysqlScanOfflineJob job = super.getById(request.getId());
        if (job == null) {
            return null;
        }
        if (job.getEnabledStatus() != null && job.getEnabledStatus() == 0) {
            throw new IllegalStateException("任务已停用，无法执行");
        }
        DbAssetMysqlScanOfflineJobInstance row = new DbAssetMysqlScanOfflineJobInstance();
        row.setTaskName(job.getTaskName());
        row.setRunStatus("waiting");
        row.setSuccessCount(0);
        row.setFailCount(0);
        row.setSensitiveCount(0);
        row.setExpectedTotal(0);
        row.setSubmittedTotal(0);
        row.setAiSuccessCount(0);
        row.setAiFailCount(0);
        row.setAiSensitiveCount(0);
        row.setAiExpectedTotal(0);
        row.setAiSubmittedTotal(0);
        row.setCreator("");
        row.setModifier("");
        row.setExtendInfo(null);
        jobInstanceService.save(row);
        return row.getId();
    }

    private void validateSave(DbAssetMysqlScanOfflineJobSaveRequest request, boolean creating) {
        if (!StringUtils.hasText(request.getTaskName())) {
            throw new IllegalArgumentException("任务名不能为空");
        }
        int sc = request.getSampleCount() != null ? request.getSampleCount() : 10;
        if (sc < SAMPLE_MIN || sc > SAMPLE_MAX) {
            throw new IllegalArgumentException("样例数必须在" + SAMPLE_MIN + "~" + SAMPLE_MAX + "之间");
        }
        LambdaQueryWrapper<DbAssetMysqlScanOfflineJob> w = new LambdaQueryWrapper<>();
        w.eq(DbAssetMysqlScanOfflineJob::getTaskName, request.getTaskName().trim());
        if (!creating) {
            w.ne(DbAssetMysqlScanOfflineJob::getId, request.getId());
        }
        long cnt = this.count(w);
        if (cnt > 0) {
            throw new IllegalArgumentException("任务名已存在");
        }
    }

    private void applyDefaults(DbAssetMysqlScanOfflineJob entity) {
        if (entity.getSampleCount() == null) {
            entity.setSampleCount(10);
        }
        if (!StringUtils.hasText(entity.getSampleMode())) {
            entity.setSampleMode("sequence");
        }
        if (entity.getEnableSampling() == null) {
            entity.setEnableSampling(1);
        }
        if (entity.getEnableAiScan() == null) {
            entity.setEnableAiScan(0);
        }
        if (!StringUtils.hasText(entity.getScanPeriod())) {
            entity.setScanPeriod("manual");
        }
        if (!StringUtils.hasText(entity.getScanScope())) {
            entity.setScanScope("all");
        }
        if (!StringUtils.hasText(entity.getTimeRangeType())) {
            entity.setTimeRangeType("full");
        }
        if (entity.getEnabledStatus() == null) {
            entity.setEnabledStatus(1);
        }
        if (entity.getTaskDescription() == null) {
            entity.setTaskDescription("");
        }
    }

    private DbAssetMysqlScanOfflineJobResponse toResponse(DbAssetMysqlScanOfflineJob entity) {
        DbAssetMysqlScanOfflineJobResponse r = new DbAssetMysqlScanOfflineJobResponse();
        BeanUtils.copyProperties(entity, r);
        return r;
    }
}
