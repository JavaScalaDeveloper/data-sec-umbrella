package com.arelore.data.sec.umbrella.server.schedule;

import com.arelore.data.sec.umbrella.server.task.TaskManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时拉取「等待运行」的离线扫描实例并分发（与接口手动触发共用 {@link TaskManager}）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OfflineScanDispatchScheduler {

    private final TaskManager taskManager;

    @Scheduled(fixedDelayString = "${offline-scan.dispatch-interval-ms:5000}")
    public void dispatchTick() {
        try {
            taskManager.dispatchOfflineMysqlScan();
        } catch (Exception e) {
            log.warn("Offline scan dispatch tick failed", e);
        }
    }
}
