package com.zzzi.springframework.jdbc.core;

import com.zzzi.springframework.jdbc.IncorrectResultSetColumnCountException;
import com.zzzi.springframework.jdbc.support.JdbcUtils;
import com.zzzi.springframework.util.NumberUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * @author zzzi
 * @date 2023/11/21 16:01
 * 第二种单行结果封装器，这里只获取单行结果中的一列，并将其类型转换之后返回
 */
public class SingleColumnRowMapper<T> implements RowMapper<T> {
    private Class<?> requiredType;

    public SingleColumnRowMapper() {
    }

    public SingleColumnRowMapper(Class<?> requiredType) {
        this.requiredType = requiredType;
    }

    @Override
    public T mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        if (columnCount != 1) {
            throw new IncorrectResultSetColumnCountException(1, columnCount);
        }
        Object result = getColumnValue(resultSet, 1, this.requiredType);
        if (result != null && this.requiredType != null && !this.requiredType.isInstance(result)) {
            try {
                return (T) convertValueToRequiredType(result, this.requiredType);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
        return (T) result;
    }

    protected Object getColumnValue(ResultSet resultSet, int index, Class<?> requiredType) throws SQLException {
        if (requiredType != null) {
            return JdbcUtils.getResultSetValue(resultSet, index, requiredType);
        } else {
            return getColumnValue(resultSet, index);
        }
    }

    protected Object getColumnValue(ResultSet resultSet, int index) throws SQLException {
        return JdbcUtils.getResultSetValue(resultSet, index);
    }

    protected Object convertValueToRequiredType(Object value, Class<?> requiredType) {
        if (String.class == requiredType)
            return value.toString();
        else if (Number.class.isAssignableFrom(requiredType)) {
            if (value instanceof Number) {
                return NumberUtils.convertNumberToTargetClass((Number) value, (Class<Number>) requiredType);
            } else {
                return NumberUtils.parseNumber(value.toString(), (Class<Number>) requiredType);
            }
        } else {
            throw new IllegalArgumentException(
                    "Value [" + value + "] is of type [" + value.getClass().getName() +
                            "] and cannot be converted to required type [" + requiredType.getName() + "]");
        }
    }
}
