package com.arelore.data.sec.umbrella.server.manager.task.asset;

import com.arelore.data.sec.umbrella.server.core.entity.DbAssetMysqlScanOfflineJob;
import com.arelore.data.sec.umbrella.server.core.entity.MySQLTableInfo;
import com.arelore.data.sec.umbrella.server.core.service.MySQLTableInfoService;
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
public class MySQLTableAssetQueryStrategy implements AssetQueryStrategy {

    private final MySQLTableInfoService tableInfoService;
    private final TaskScopeHelper scopeHelper;

    @Override
    public long total(DbAssetMysqlScanOfflineJob job) {
        LambdaQueryWrapper<MySQLTableInfo> w = buildWrapper(job);
        return tableInfoService.count(w);
    }

    @Override
    public AssetPage page(DbAssetMysqlScanOfflineJob job, long current, long size) {
        LambdaQueryWrapper<MySQLTableInfo> w = buildWrapper(job);
        Page<MySQLTableInfo> page = new Page<>(current, size);
        IPage<MySQLTableInfo> r = tableInfoService.page(page, w);
        List<Map<String, Object>> records = r.getRecords().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return new AssetPage(r.getTotal(), records);
    }

    private LambdaQueryWrapper<MySQLTableInfo> buildWrapper(DbAssetMysqlScanOfflineJob job) {
        LambdaQueryWrapper<MySQLTableInfo> w = new LambdaQueryWrapper<>();
        // 扫描范围：all / instance
        String scope = job.getScanScope();
        if (StringUtils.hasText(scope) && "instance".equalsIgnoreCase(scope.trim())) {
            List<String> instances = scopeHelper.parseStringList(job.getScanInstanceIds());
            if (instances.isEmpty()) {
                // 返回空集合：让上层判定为无资产
                w.eq(MySQLTableInfo::getId, -1);
            } else {
                w.in(MySQLTableInfo::getInstance, instances);
            }
        }
        w.orderByAsc(MySQLTableInfo::getId);
        return w;
    }

    private Map<String, Object> toMap(MySQLTableInfo t) {
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
        m.put("manualSensitive", t.getManualSensitive());
        m.put("columnInfo", t.getColumnInfo());
        m.put("columnScanInfo", t.getColumnScanInfo());
        m.put("columnAiScanInfo", t.getColumnAiScanInfo());
        return m;
    }
}

