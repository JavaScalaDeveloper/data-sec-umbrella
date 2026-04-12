package com.arelore.data.sec.umbrella.server.worker.ai;

import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineDatabaseScanDispatchPayload;
import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyAssetSample;
import com.arelore.data.sec.umbrella.server.core.service.checker.RulesChecker;
import com.arelore.data.sec.umbrella.server.core.service.factory.RulesCheckerFactory;
import com.arelore.data.sec.umbrella.server.worker.mq.OfflineScanSnapshotPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * ClickHouse 资产：当前无独立表资产表回写，仅基于 MQ 资产字段做 AI 评估与快照发布（与规则侧 {@link com.arelore.data.sec.umbrella.server.worker.scanner.ClickhouseAssetScanner} 占位策略一致，可后续扩展持久化）。
 */
@Slf4j
@Component
public class ClickhouseOfflineAiAssetHandler implements OfflineAiAssetHandler {

    private final OfflineScanSnapshotPublisher offlineScanSnapshotPublisher;
    private final RulesChecker rulesChecker;

    public ClickhouseOfflineAiAssetHandler(OfflineScanSnapshotPublisher offlineScanSnapshotPublisher) {
        this.offlineScanSnapshotPublisher = offlineScanSnapshotPublisher;
        RulesChecker c = RulesCheckerFactory.getRulesChecker("Clickhouse");
        if (c == null) {
            throw new IllegalStateException("RulesCheckerFactory 未注册 Clickhouse");
        }
        this.rulesChecker = c;
    }

    @Override
    public String databaseType() {
        return "Clickhouse";
    }

    @Override
    public AiAssetProcessOutcome processOne(OfflineDatabaseScanDispatchPayload payload, Map<String, Object> asset) {
        List<DatabasePolicyAssetSample> samples = OfflineAiScanSupport.parseSamples(asset.get("columnSamples"));
        OfflineAiScanSupport.AiScanResult aiResult = OfflineAiScanSupport.evaluateAi(
                rulesChecker, samples, payload.getPolicies());
        String dataInstance = OfflineAiScanSupport.str(asset.get("instance"));
        String databaseName = OfflineAiScanSupport.str(asset.get("databaseName"));
        String tableName = OfflineAiScanSupport.str(asset.get("tableName"));
        if (!StringUtils.hasText(dataInstance) || !StringUtils.hasText(databaseName) || !StringUtils.hasText(tableName)) {
            log.warn("Clickhouse AI 扫描缺少 instance/databaseName/tableName，跳过快照发布, asset={}", asset);
            return new AiAssetProcessOutcome(aiResult.llmInvocationFailed(), aiResult.maxLevel() > 0);
        }
        String engine = payload.getEngine() != null && !payload.getEngine().isBlank() ? payload.getEngine() : "Clickhouse";
        OfflineAiScanSupport.publishAiScanSnapshot(
                payload,
                engine,
                dataInstance,
                databaseName,
                tableName,
                aiResult,
                offlineScanSnapshotPublisher
        );
        return new AiAssetProcessOutcome(aiResult.llmInvocationFailed(), aiResult.maxLevel() > 0);
    }
}
