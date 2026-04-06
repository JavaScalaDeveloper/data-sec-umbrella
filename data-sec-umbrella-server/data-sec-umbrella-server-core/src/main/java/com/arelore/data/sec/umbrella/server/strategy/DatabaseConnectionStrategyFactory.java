package com.arelore.data.sec.umbrella.server.strategy;

import com.arelore.data.sec.umbrella.server.strategy.impl.MySQLConnectionStrategy;
import com.arelore.data.sec.umbrella.server.strategy.impl.OracleConnectionStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库连接策略工厂类
 */
public class DatabaseConnectionStrategyFactory {

    private static final Map<String, DatabaseConnectionStrategy> STRATEGY_MAP = new HashMap<>();

    static {
        // 初始化策略映射
        STRATEGY_MAP.put("MySQL", new MySQLConnectionStrategy());
        STRATEGY_MAP.put("Oracle", new OracleConnectionStrategy());
    }

    /**
     * 根据数据库类型获取对应的连接策略
     */
    public static DatabaseConnectionStrategy getStrategy(String databaseType) {
        return STRATEGY_MAP.getOrDefault(databaseType, null);
    }
}
