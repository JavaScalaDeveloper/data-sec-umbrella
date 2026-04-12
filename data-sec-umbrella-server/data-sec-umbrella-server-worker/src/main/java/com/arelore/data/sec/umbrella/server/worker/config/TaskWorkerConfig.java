package com.arelore.data.sec.umbrella.server.worker.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({TaskWorkerPoolProperties.class, TaskWorkerAiPoolProperties.class})
/**
 * Worker 配置装配类。
 *
 * @author 黄佳豪
 */
public class TaskWorkerConfig {
}

