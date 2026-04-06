package com.arelore.data.sec.umbrella.server.core.constant;

/**
 * 离线扫描 MQ / Redis 约定
 */
public final class OfflineScanConstants {

    private OfflineScanConstants() {
    }

    // RabbitMQ（manager 投递，worker 消费）
    public static final String RABBIT_EXCHANGE = "offline.mysql.scan.exchange";
    public static final String RABBIT_QUEUE = "offline.mysql.scan.queue";
    public static final String RABBIT_ROUTING_KEY = "OFFLINE_MYSQL_SCAN";

    public static final String REDIS_KEY_INSTANCE_PREFIX = "offline-scan:instance:";
    public static final String REDIS_KEY_LAST_INSTANCE = "offline-scan:last-instance-id";
    public static final String REDIS_KEY_INSTANCE_VERSION_SUFFIX = ":version";
}
