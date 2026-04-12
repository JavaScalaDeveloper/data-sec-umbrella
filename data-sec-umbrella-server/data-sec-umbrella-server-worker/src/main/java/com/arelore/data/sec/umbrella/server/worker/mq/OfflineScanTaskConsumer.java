package com.arelore.data.sec.umbrella.server.worker.mq;

import com.alibaba.fastjson2.JSON;
import com.arelore.data.sec.umbrella.server.core.constant.OfflineScanConstants;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineDatabaseScanDispatchPayload;
import com.arelore.data.sec.umbrella.server.worker.config.TaskWorkerExecutorBeans;
import com.arelore.data.sec.umbrella.server.worker.executor.TaskWorkerExecutorManager;
import com.arelore.data.sec.umbrella.server.worker.task.OfflineScanTaskProcessor;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

@Slf4j
@Component
/**
 * RabbitMQ 离线扫描消费者。
 *
 * @author 黄佳豪
 */
public class OfflineScanTaskConsumer {

    private final OfflineScanTaskProcessor offlineRuleScanTaskProcessor;
    private final TaskWorkerExecutorManager executorManager;
    private final StringRedisTemplate stringRedisTemplate;

    public OfflineScanTaskConsumer(@Qualifier("offlineRuleScanTaskProcessor") OfflineScanTaskProcessor offlineRuleScanTaskProcessor,
                                   @Qualifier(TaskWorkerExecutorBeans.RULE_SCAN_EXECUTOR) TaskWorkerExecutorManager executorManager,
                                   StringRedisTemplate stringRedisTemplate) {
        this.offlineRuleScanTaskProcessor = offlineRuleScanTaskProcessor;
        this.executorManager = executorManager;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @RabbitListener(queues = OfflineScanConstants.RABBIT_QUEUE)
    /**
     * 消费离线扫描消息并执行手动 ACK。
     *
     * @param message RabbitMQ 消息体
     * @param channel RabbitMQ 信道
     * @param deliveryTag 投递标签
     * @throws Exception 消费异常
     */
    public void onMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws Exception {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        OfflineDatabaseScanDispatchPayload payload = JSON.parseObject(body, OfflineDatabaseScanDispatchPayload.class);
        if (!isMessageVersionValid(payload)) {
            // 版本不一致：说明是旧任务消息，直接 ACK 丢弃
            channel.basicAck(deliveryTag, false);
            return;
        }
        try {
            Future<?> f = executorManager.submit(() -> offlineRuleScanTaskProcessor.process(payload));
            f.get();
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            log.error("consume offline scan msg failed, body={}", body, ex);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private boolean isMessageVersionValid(OfflineDatabaseScanDispatchPayload payload) {
        if (payload == null || payload.getInstanceId() == null || payload.getDispatchVersion() == null) {
            return false;
        }
        String key = OfflineScanConstants.REDIS_KEY_INSTANCE_PREFIX
                + payload.getInstanceId()
                + OfflineScanConstants.REDIS_KEY_INSTANCE_VERSION_SUFFIX;
        String currentVersion = stringRedisTemplate.opsForValue().get(key);
        if (currentVersion == null) {
            return false;
        }
        return String.valueOf(payload.getDispatchVersion()).equals(currentVersion);
    }
}

