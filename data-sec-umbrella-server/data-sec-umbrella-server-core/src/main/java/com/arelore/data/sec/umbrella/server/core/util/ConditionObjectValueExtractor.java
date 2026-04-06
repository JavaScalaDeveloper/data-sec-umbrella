package com.arelore.data.sec.umbrella.server.core.util;

import com.arelore.data.sec.umbrella.server.core.dto.request.DatabasePolicyTestRulesRequest;

/**
 * 条件对象值提取工具类
 */
public class ConditionObjectValueExtractor {
    
    /**
     * 根据条件对象获取对应的值
     */
    public static String getValueByConditionObject(DatabasePolicyTestRulesRequest.TestData data, String conditionObject) {
        if (data == null || conditionObject == null) {
            return null;
        }
        
        switch (conditionObject) {
            case "库名":
                return data.getDatabaseName();
            case "库描述":
                return data.getDatabaseDescription();
            case "表名":
                return data.getTableName();
            case "表描述":
                return data.getTableDescription();
            case "列名":
                return data.getColumnName();
            case "列描述":
                return data.getColumnDescription();
            case "列值":
                // 如果是列值，返回第一个列值用于测试
                if (data.getColumnValues() != null && !data.getColumnValues().isEmpty()) {
                    return data.getColumnValues().get(0);
                }
                return null;
            default:
                return null;
        }
    }
}