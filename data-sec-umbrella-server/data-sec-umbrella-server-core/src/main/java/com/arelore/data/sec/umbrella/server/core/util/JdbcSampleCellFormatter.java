package com.arelore.data.sec.umbrella.server.core.util;

import com.alibaba.fastjson2.JSON;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * JDBC {@link java.sql.ResultSet#getObject(int)} 取值转样例展示字符串：
 * 数组 / 集合 / Map 等用 JSON，避免 {@code String.valueOf} 出现 {@code [Ljava.lang.String;@...}。
 */
public final class JdbcSampleCellFormatter {

    private JdbcSampleCellFormatter() {
    }

    public static String toSampleString(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Array) {
            try {
                Object arr = ((Array) value).getArray();
                if (arr == null) {
                    return "";
                }
                return JSON.toJSONString(arr);
            } catch (SQLException e) {
                return String.valueOf(value);
            }
        }
        if (value instanceof byte[]) {
            return Base64.getEncoder().encodeToString((byte[]) value);
        }
        Class<?> c = value.getClass();
        if (c.isArray()) {
            return JSON.toJSONString(value);
        }
        if (value instanceof List<?> || value instanceof Map<?, ?>) {
            return JSON.toJSONString(value);
        }
        return String.valueOf(value);
    }
}
