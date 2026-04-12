package com.arelore.data.sec.umbrella.server.worker.config;

import com.arelore.data.sec.umbrella.server.worker.executor.TaskWorkerExecutorManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
/**
 * 规则扫描与 AI 扫描使用独立线程池，避免 Ollama 等长任务占满规则扫描线程。
 *
 * @author 黄佳豪
 */
public class TaskWorkerExecutorBeans {

    public static final String RULE_SCAN_EXECUTOR = "offlineRuleScanExecutorManager";
    public static final String AI_SCAN_EXECUTOR = "offlineAiScanExecutorManager";

    @Bean(RULE_SCAN_EXECUTOR)
    public TaskWorkerExecutorManager offlineRuleScanExecutorManager(TaskWorkerPoolProperties props) {
        return new TaskWorkerExecutorManager(props, "offline-rule-scan");
    }

    @Bean(AI_SCAN_EXECUTOR)
    public TaskWorkerExecutorManager offlineAiScanExecutorManager(TaskWorkerAiPoolProperties props) {
        return new TaskWorkerExecutorManager(props, "offline-ai-scan");
    }
}
