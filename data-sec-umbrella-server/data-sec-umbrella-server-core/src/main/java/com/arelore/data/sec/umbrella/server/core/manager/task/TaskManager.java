package com.arelore.data.sec.umbrella.server.core.manager.task;

/**
 * 任务分发入口：由管理端定时调度或业务 Service 在合适时机触发。
 *
 * @author 黄佳豪
 */
public interface TaskManager {

    /**
     * 处理「等待运行」的 MySQL 离线扫描实例：校验规则、拉取资产、投递 MQ、写入 Redis。
     *
     * @return void
     */
    void dispatchOfflineMysqlScan();
}
