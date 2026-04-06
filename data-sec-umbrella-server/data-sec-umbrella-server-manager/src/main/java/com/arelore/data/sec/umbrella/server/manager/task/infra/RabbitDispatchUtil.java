package com.arelore.data.sec.umbrella.server.manager.task.infra;

import com.arelore.data.sec.umbrella.server.core.constant.OfflineScanConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitDispatchUtil {

    private final RabbitTemplate rabbitTemplate;

    public void sendOfflineScan(String body) {
        rabbitTemplate.convertAndSend(
                OfflineScanConstants.RABBIT_EXCHANGE,
                OfflineScanConstants.RABBIT_ROUTING_KEY,
                body
        );
    }
}

