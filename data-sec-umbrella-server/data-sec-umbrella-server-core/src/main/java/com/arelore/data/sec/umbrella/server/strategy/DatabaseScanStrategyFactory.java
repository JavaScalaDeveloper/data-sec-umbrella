package com.arelore.data.sec.umbrella.server.strategy;

import com.arelore.data.sec.umbrella.server.strategy.impl.MySQLScanStrategy;
import com.arelore.data.sec.umbrella.server.strategy.impl.OracleScanStrategy;
import com.arelore.data.sec.umbrella.server.strategy.impl.SQLServerScanStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库扫描策略工厂
 */
public class DatabaseScanStrategyFactory {

    private static final Map<String, DatabaseScanStrategy> STRATEGY_MAP = new HashMap<>();

    static {
        // 注册各种数据库扫描策略
        STRATEGY_MAP.put("MySQL", new MySQLScanStrategy());
        STRATEGY_MAP.put("Oracle", new OracleScanStrategy());
        STRATEGY_MAP.put("SQL Server", new SQLServerScanStrategy());
    }

    /**
     * 根据数据库类型获取扫描策略
     *
     * @param databaseType 数据库类型
     * @return 数据库扫描策略
     */
    public static DatabaseScanStrategy getStrategy(String databaseType) {
        DatabaseScanStrategy strategy = STRATEGY_MAP.get(databaseType);
        // 如果没有找到对应的策略，返回MySQL策略作为默认值
        return strategy != null ? strategy : new MySQLScanStrategy();
    }
}
