package com.arelore.data.sec.umbrella.server.manager.task.asset;

import com.arelore.data.sec.umbrella.server.core.entity.mysql.ClickhouseTableInfo;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.DbAssetMysqlScanOfflineJob;
import com.arelore.data.sec.umbrella.server.core.service.ClickhouseTableInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ClickhouseTableAssetQueryStrategy implements AssetQueryStrategy {

    private final ClickhouseTableInfoService tableInfoService;
    private final TaskScopeHelper scopeHelper;

    @Override
    public long total(DbAssetMysqlScanOfflineJob job) {
        LambdaQueryWrapper<ClickhouseTableInfo> w = buildWrapper(job);
        return tableInfoService.count(w);
    }

    @Override
    public AssetPage page(DbAssetMysqlScanOfflineJob job, long current, long size) {
        LambdaQueryWrapper<ClickhouseTableInfo> w = buildWrapper(job);
        Page<ClickhouseTableInfo> page = new Page<>(current, size);
        IPage<ClickhouseTableInfo> r = tableInfoService.page(page, w);
        List<Map<String, Object>> records = r.getRecords().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return new AssetPage(r.getTotal(), records);
    }

    private LambdaQueryWrapper<ClickhouseTableInfo> buildWrapper(DbAssetMysqlScanOfflineJob job) {
        LambdaQueryWrapper<ClickhouseTableInfo> w = new LambdaQueryWrapper<>();
        String scope = job.getScanScope();
        if (StringUtils.hasText(scope) && "instance".equalsIgnoreCase(scope.trim())) {
            List<String> instances = scopeHelper.parseStringList(job.getScanInstanceIds());
            if (instances.isEmpty()) {
                w.eq(ClickhouseTableInfo::getId, -1);
            } else {
                w.in(ClickhouseTableInfo::getInstance, instances);
            }
        }
        w.orderByAsc(ClickhouseTableInfo::getId);
        return w;
    }

    private Map<String, Object> toMap(ClickhouseTableInfo t) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", t.getId());
        m.put("instance", t.getInstance());
        m.put("databaseName", t.getDatabaseName());
        m.put("tableName", t.getTableName());
        m.put("description", t.getDescription());
        m.put("sensitivityLevel", t.getSensitivityLevel());
        m.put("sensitivityTags", t.getSensitivityTags());
        m.put("aiSensitivityLevel", t.getAiSensitivityLevel());
        m.put("aiSensitivityTags", t.getAiSensitivityTags());
        m.put("manualReview", t.getManualReview());
        m.put("columnInfo", t.getColumnInfo());
        m.put("columnScanInfo", t.getColumnScanInfo());
        m.put("columnAiScanInfo", t.getColumnAiScanInfo());
        return m;
    }
}
