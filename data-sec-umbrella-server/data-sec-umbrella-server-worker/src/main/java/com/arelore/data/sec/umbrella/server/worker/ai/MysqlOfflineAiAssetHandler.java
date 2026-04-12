package com.arelore.data.sec.umbrella.server.worker.ai;

import com.alibaba.fastjson2.JSON;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineDatabaseScanDispatchPayload;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyAssetSample;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.MySQLTableInfo;
import com.arelore.data.sec.umbrella.server.core.service.MySQLTableInfoService;
import com.arelore.data.sec.umbrella.server.core.service.checker.RulesChecker;
import com.arelore.data.sec.umbrella.server.core.service.factory.RulesCheckerFactory;
import com.arelore.data.sec.umbrella.server.worker.mq.OfflineScanSnapshotPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MySQL 表资产：从 {@code db_asset_mysql_table_info} 加载并回写 AI 字段。
 */
@Component
public class MysqlOfflineAiAssetHandler implements OfflineAiAssetHandler {

    private final MySQLTableInfoService mySQLTableInfoService;
    private final OfflineScanSnapshotPublisher offlineScanSnapshotPublisher;
    private final RulesChecker rulesChecker;

    public MysqlOfflineAiAssetHandler(
            MySQLTableInfoService mySQLTableInfoService,
            OfflineScanSnapshotPublisher offlineScanSnapshotPublisher) {
        this.mySQLTableInfoService = mySQLTableInfoService;
        this.offlineScanSnapshotPublisher = offlineScanSnapshotPublisher;
        RulesChecker c = RulesCheckerFactory.getRulesChecker("MySQL");
        if (c == null) {
            throw new IllegalStateException("RulesCheckerFactory 未注册 MySQL");
        }
        this.rulesChecker = c;
    }

    @Override
    public String databaseType() {
        return "MySQL";
    }

    @Override
    public AiAssetProcessOutcome processOne(OfflineDatabaseScanDispatchPayload payload, Map<String, Object> asset) {
        Long assetId = OfflineAiScanSupport.toAssetId(asset);
        if (assetId == null) {
            return null;
        }
        MySQLTableInfo tableInfo = mySQLTableInfoService.getById(assetId);
        if (tableInfo == null) {
            return null;
        }
        List<DatabasePolicyAssetSample> samples = OfflineAiScanSupport.parseSamples(asset.get("columnSamples"));
        OfflineAiScanSupport.AiScanResult aiResult = OfflineAiScanSupport.evaluateAi(
                rulesChecker, samples, payload.getPolicies());
        tableInfo.setAiSensitivityLevel(String.valueOf(aiResult.maxLevel()));
        tableInfo.setAiSensitivityTags(String.join(",", aiResult.tags()));
        tableInfo.setColumnAiScanInfo(JSON.toJSONString(aiResult.columnInfo()));
        mySQLTableInfoService.updateById(tableInfo);
        String engine = payload.getEngine() != null && !payload.getEngine().isBlank() ? payload.getEngine() : "MySQL";
        OfflineAiScanSupport.publishAiScanSnapshot(
                payload,
                engine,
                tableInfo.getInstance(),
                tableInfo.getDatabaseName(),
                tableInfo.getTableName(),
                aiResult,
                offlineScanSnapshotPublisher
        );
        return new AiAssetProcessOutcome(aiResult.llmInvocationFailed(), aiResult.maxLevel() > 0);
    }
}
