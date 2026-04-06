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
}

