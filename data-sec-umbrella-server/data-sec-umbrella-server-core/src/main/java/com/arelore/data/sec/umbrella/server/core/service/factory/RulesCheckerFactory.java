package com.arelore.data.sec.umbrella.server.core.service.factory;

import com.arelore.data.sec.umbrella.server.core.service.checker.MySQLRulesChecker;
import com.arelore.data.sec.umbrella.server.core.service.checker.ClickhouseRulesChecker;
import com.arelore.data.sec.umbrella.server.core.service.checker.PostgreSQLRulesChecker;
import com.arelore.data.sec.umbrella.server.core.service.checker.OracleRulesChecker;
import com.arelore.data.sec.umbrella.server.core.service.checker.RulesChecker;

import java.util.HashMap;
import java.util.Map;

/**
 * 规则检查器工厂类
 */
public class RulesCheckerFactory {
    
    private static final Map<String, RulesChecker> RULES_CHECKER_MAP = new HashMap<>();
    
    static {
        // 初始化规则检查器映射表
        RULES_CHECKER_MAP.put("MySQL", new MySQLRulesChecker());
        RULES_CHECKER_MAP.put("Clickhouse", new ClickhouseRulesChecker());
        RULES_CHECKER_MAP.put("PostgreSQL", new PostgreSQLRulesChecker());
        RULES_CHECKER_MAP.put("Oracle", new OracleRulesChecker());
    }
    
    /**
     * 根据数据库类型获取对应的规则检查器
     */
    public static RulesChecker getRulesChecker(String databaseType) {
        return RULES_CHECKER_MAP.get(databaseType);
    }
}