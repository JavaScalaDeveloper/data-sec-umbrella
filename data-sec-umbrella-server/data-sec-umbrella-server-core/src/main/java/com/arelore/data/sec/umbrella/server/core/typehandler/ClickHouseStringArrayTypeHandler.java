package com.arelore.data.sec.umbrella.server.core.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ClickHouse {@code Array(String)} / JDBC 数组 → {@code List<String>}（只实现读路径）。
 */
@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.ARRAY)
public class ClickHouseStringArrayTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) {
        throw new UnsupportedOperationException("read-only");
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return convert(rs.getObject(columnName));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return convert(rs.getObject(columnIndex));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return convert(cs.getObject(columnIndex));
    }

    private static List<String> convert(Object v) throws SQLException {
        if (v == null) {
            return Collections.emptyList();
        }
        if (v instanceof List<?> list) {
            List<String> out = new ArrayList<>(list.size());
            for (Object o : list) {
                out.add(o == null ? "" : String.valueOf(o));
            }
            return out;
        }
        if (v instanceof String[] arr) {
            List<String> out = new ArrayList<>(arr.length);
            for (String s : arr) {
                out.add(s == null ? "" : s);
            }
            return out;
        }
        if (v instanceof Object[] arr) {
            List<String> out = new ArrayList<>(arr.length);
            for (Object o : arr) {
                out.add(o == null ? "" : String.valueOf(o));
            }
            return out;
        }
        if (v instanceof Array sqlArr) {
            Object arr = sqlArr.getArray();
            if (arr instanceof Object[] oa) {
                List<String> out = new ArrayList<>(oa.length);
                for (Object o : oa) {
                    out.add(o == null ? "" : String.valueOf(o));
                }
                return out;
            }
        }
        return List.of(String.valueOf(v));
    }
}
