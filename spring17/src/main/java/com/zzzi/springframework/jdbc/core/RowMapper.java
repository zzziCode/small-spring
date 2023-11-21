package com.zzzi.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author zzzi
 * @date 2023/11/21 15:42
 * 实现这个接口，在内部定义如何处理单行数据
 */
public interface RowMapper<T> {
    T mapRow(ResultSet resultSet, int rowNum) throws SQLException;
}
