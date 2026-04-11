package com.arelore.data.sec.umbrella.server.worker.mq;

import com.alibaba.fastjson2.JSON;
import com.arelore.data.sec.umbrella.server.core.constant.OfflineScanConstants;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineDatabaseScanDispatchPayload;
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

/**
 * RabbitMQ 离线 AI 扫描消费者。
 *
 * @author 黄佳豪
 */
@Slf4j
@Component
public class OfflineAiScanTaskConsumer {

    private final OfflineScanTaskProcessor offlineAiScanTaskProcessor;
    private final TaskWorkerExecutorManager executorManager;
    private final StringRedisTemplate stringRedisTemplate;

    public OfflineAiScanTaskConsumer(@Qualifier("offlineAiScanTaskProcessor") OfflineScanTaskProcessor offlineAiScanTaskProcessor,
                                     TaskWorkerExecutorManager executorManager,
                                     StringRedisTemplate stringRedisTemplate) {
        this.offlineAiScanTaskProcessor = offlineAiScanTaskProcessor;
        this.executorManager = executorManager;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @RabbitListener(queues = OfflineScanConstants.RABBIT_AI_QUEUE)
    public void onMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws Exception {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        OfflineDatabaseScanDispatchPayload payload = JSON.parseObject(body, OfflineDatabaseScanDispatchPayload.class);
        if (!isMessageVersionValid(payload)) {
            channel.basicAck(deliveryTag, false);
            return;
        }
        try {
            Future<?> f = executorManager.submit(() -> offlineAiScanTaskProcessor.process(payload));
            f.get();
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            log.error("consume offline ai scan msg failed, body={}", body, ex);
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
        return currentVersion != null && String.valueOf(payload.getDispatchVersion()).equals(currentVersion);
    }
}
