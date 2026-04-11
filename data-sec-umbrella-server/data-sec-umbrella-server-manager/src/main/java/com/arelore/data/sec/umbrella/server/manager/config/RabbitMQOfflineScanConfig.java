package com.arelore.data.sec.umbrella.server.manager.config;

import com.arelore.data.sec.umbrella.server.core.constant.OfflineScanConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 离线扫描 RabbitMQ 拓扑：Exchange + Queue + Binding
 */
@Configuration
public class RabbitMQOfflineScanConfig {

    @Bean
    public DirectExchange offlineScanExchange() {
        return new DirectExchange(OfflineScanConstants.RABBIT_EXCHANGE, true, false);
    }

    @Bean
    public Queue offlineScanQueue() {
        return QueueBuilder.durable(OfflineScanConstants.RABBIT_QUEUE).build();
    }

    @Bean
    public Queue offlineAiScanQueue() {
        return QueueBuilder.durable(OfflineScanConstants.RABBIT_AI_QUEUE).build();
    }

    @Bean
    public Binding offlineScanBinding(DirectExchange offlineScanExchange, Queue offlineScanQueue) {
        return BindingBuilder.bind(offlineScanQueue)
                .to(offlineScanExchange)
                .with(OfflineScanConstants.RABBIT_ROUTING_KEY);
    }

    @Bean
    public Binding offlineAiScanBinding(DirectExchange offlineScanExchange, Queue offlineAiScanQueue) {
        return BindingBuilder.bind(offlineAiScanQueue)
                .to(offlineScanExchange)
                .with(OfflineScanConstants.RABBIT_AI_ROUTING_KEY);
    }

    @Bean
    public Queue offlineScanSnapshotTableQueue() {
        return QueueBuilder.durable(OfflineScanConstants.RABBIT_SNAPSHOT_TABLE_QUEUE).build();
    }

    @Bean
    public Queue offlineScanSnapshotColumnQueue() {
        return QueueBuilder.durable(OfflineScanConstants.RABBIT_SNAPSHOT_COLUMN_QUEUE).build();
    }

    @Bean
    public Binding offlineScanSnapshotTableBinding(DirectExchange offlineScanExchange, Queue offlineScanSnapshotTableQueue) {
        return BindingBuilder.bind(offlineScanSnapshotTableQueue)
                .to(offlineScanExchange)
                .with(OfflineScanConstants.RABBIT_SNAPSHOT_TABLE_ROUTING_KEY);
    }

    @Bean
    public Binding offlineScanSnapshotColumnBinding(DirectExchange offlineScanExchange, Queue offlineScanSnapshotColumnQueue) {
        return BindingBuilder.bind(offlineScanSnapshotColumnQueue)
                .to(offlineScanExchange)
                .with(OfflineScanConstants.RABBIT_SNAPSHOT_COLUMN_ROUTING_KEY);
    }
}

