package com.zzzi.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author zzzi
 * @date 2023/11/21 16:02
 * 实现这个接口的类在extractData方法中将结果集封装成一个List
 * 封装时针对结果集中的每一行，都调用实现了RowMapper的单行结果封装器
 */
public interface ResultSetExtractor<T> {
    T extractData(ResultSet resultSet) throws SQLException;
}
