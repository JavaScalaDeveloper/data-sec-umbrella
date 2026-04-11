package com.arelore.data.sec.umbrella.server.worker.mq;

import com.alibaba.fastjson2.JSON;
import com.arelore.data.sec.umbrella.server.core.constant.OfflineScanConstants;
import com.arelore.data.sec.umbrella.server.core.dto.messaging.OfflineScanSensitivitySnapshotMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 将扫描快照发往 MQ：表级、字段级各一路由（Topic/路由键分离），由下游分别落 ClickHouse 等。
 */
@Component
@RequiredArgsConstructor
public class OfflineScanSnapshotPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishTableSnapshot(OfflineScanSensitivitySnapshotMessage message) {
        if (message == null) {
            return;
        }
        rabbitTemplate.convertAndSend(
                OfflineScanConstants.RABBIT_EXCHANGE,
                OfflineScanConstants.RABBIT_SNAPSHOT_TABLE_ROUTING_KEY,
                JSON.toJSONString(message)
        );
    }

    public void publishColumnSnapshot(OfflineScanSensitivitySnapshotMessage message) {
        if (message == null) {
            return;
        }
        rabbitTemplate.convertAndSend(
                OfflineScanConstants.RABBIT_EXCHANGE,
                OfflineScanConstants.RABBIT_SNAPSHOT_COLUMN_ROUTING_KEY,
                JSON.toJSONString(message)
        );
    }
}
