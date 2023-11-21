package com.zzzi.springframework.jdbc.core;

import com.zzzi.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zzzi
 * @date 2023/11/21 16:00
 * 第一种单行结果封装器，将单行结果中的每一列都封装成Map中的一个键值对
 */
public class ColumnMapRowMapper implements RowMapper<Map<String, Object>> {
    @Override
    public Map<String, Object> mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        //创建一个指定大小的map来存储所有列数据
        Map<String, Object> columnMap = createColumnMap(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            String columnName = JdbcUtils.lookupColumnName(metaData, i);
            columnMap.putIfAbsent(columnName, getColumnValue(resultSet, i));
        }
        return columnMap;
    }

    protected Map<String, Object> createColumnMap(int columnCount) {
        return new LinkedHashMap<>(columnCount);
    }

    protected Object getColumnValue(ResultSet resultSet, int index) throws SQLException {
        return JdbcUtils.getResultSetValue(resultSet, index);
    }
}
