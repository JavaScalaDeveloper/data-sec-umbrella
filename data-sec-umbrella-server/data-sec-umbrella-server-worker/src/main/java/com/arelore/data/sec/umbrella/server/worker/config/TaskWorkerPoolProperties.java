package com.arelore.data.sec.umbrella.server.worker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "offline-scan.worker.pool")
/**
 * Worker 线程池配置项。
 *
 * @author 黄佳豪
 */
public class TaskWorkerPoolProperties {
    private int coreSize = 8;
    private int maxSize = 32;
    private int queueCapacity = 500;
    private int keepAliveSeconds = 60;
}

