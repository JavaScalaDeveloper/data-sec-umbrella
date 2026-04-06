package com.arelore.data.sec.umbrella.server.config;

import com.arelore.data.sec.umbrella.server.constant.OfflineScanConstants;
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
    public Binding offlineScanBinding(DirectExchange offlineScanExchange, Queue offlineScanQueue) {
        return BindingBuilder.bind(offlineScanQueue)
                .to(offlineScanExchange)
                .with(OfflineScanConstants.RABBIT_ROUTING_KEY);
    }
}

