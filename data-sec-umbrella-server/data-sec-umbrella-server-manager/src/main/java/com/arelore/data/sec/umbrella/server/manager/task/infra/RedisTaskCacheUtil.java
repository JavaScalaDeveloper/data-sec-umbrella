package com.arelore.data.sec.umbrella.server.manager.task.infra;

import com.arelore.data.sec.umbrella.server.core.constant.OfflineScanConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisTaskCacheUtil {

    private final StringRedisTemplate stringRedisTemplate;

    public void cacheInstanceToJob(Long instanceId, Long jobId, Long dispatchVersion) {
        stringRedisTemplate.opsForValue().set(
                OfflineScanConstants.REDIS_KEY_INSTANCE_PREFIX + instanceId,
                String.valueOf(jobId),
                Duration.ofDays(7)
        );
        stringRedisTemplate.opsForValue().set(
                OfflineScanConstants.REDIS_KEY_INSTANCE_PREFIX + instanceId + OfflineScanConstants.REDIS_KEY_INSTANCE_VERSION_SUFFIX,
                String.valueOf(dispatchVersion),
                Duration.ofDays(7)
        );
        stringRedisTemplate.opsForValue().set(
                OfflineScanConstants.REDIS_KEY_LAST_INSTANCE,
                String.valueOf(instanceId),
                Duration.ofDays(7)
        );
    }

    /**
     * 将实例的派发版本号更新为新值，使已入队但携带旧 {@code dispatchVersion} 的 MQ 消息在 Worker
     * {@code isMessageVersionValid} 校验失败并被 ACK 丢弃。
     */
    public void bumpDispatchVersion(Long instanceId) {
        if (instanceId == null) {
            return;
        }
        long v = System.currentTimeMillis();
        stringRedisTemplate.opsForValue().set(
                OfflineScanConstants.REDIS_KEY_INSTANCE_PREFIX + instanceId + OfflineScanConstants.REDIS_KEY_INSTANCE_VERSION_SUFFIX,
                String.valueOf(v),
                Duration.ofDays(7)
        );
    }
}

