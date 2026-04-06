package com.arelore.data.sec.umbrella.server.worker.executor;

import com.arelore.data.sec.umbrella.server.worker.config.TaskWorkerPoolProperties;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
/**
 * Worker 线程池管理器，负责任务提交与动态参数调整。
 *
 * @author 黄佳豪
 */
public class TaskWorkerExecutorManager {

    private final ThreadPoolExecutor executor;

    public TaskWorkerExecutorManager(TaskWorkerPoolProperties props) {
        this.executor = new ThreadPoolExecutor(
                props.getCoreSize(),
                props.getMaxSize(),
                props.getKeepAliveSeconds(),
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(props.getQueueCapacity()),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    /**
     * 动态更新线程池参数。
     *
     * @param coreSize 核心线程数
     * @param maxSize 最大线程数
     * @param queueCapacity 队列容量（当前实现仅作参数校验）
     * @param keepAliveSeconds 非核心线程存活时间（秒）
     */
    public synchronized void updatePool(int coreSize, int maxSize, int queueCapacity, int keepAliveSeconds) {
        if (coreSize <= 0 || maxSize <= 0 || maxSize < coreSize || queueCapacity <= 0 || keepAliveSeconds < 0) {
            throw new IllegalArgumentException("线程池参数非法");
        }
        // 运行时仅安全调整可变参数，队列容量不可热更新（该参数用于后续重建时生效）
        executor.setMaximumPoolSize(maxSize);
        executor.setCorePoolSize(coreSize);
        executor.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
    }

    @PreDestroy
    /**
     * 应用关闭时优雅停止线程池。
     */
    public void shutdown() {
        executor.shutdown();
    }
}

