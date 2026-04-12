package com.arelore.data.sec.umbrella.server.worker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "offline-scan.worker.pool-ai")
/**
 * 离线 AI 扫描专用线程池配置（与规则扫描 {@link TaskWorkerPoolProperties} 隔离）。
 *
 * @author 黄佳豪
 */
public class TaskWorkerAiPoolProperties {
    private int coreSize = 4;
    private int maxSize = 16;
    private int queueCapacity = 200;
    private int keepAliveSeconds = 60;
}
