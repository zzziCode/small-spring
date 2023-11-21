package com.zzzi.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author zzzi
 * @date 2023/11/21 16:46
 * 实现这个接口的类在内部执行SQL语句并且将结果进行封装
 */
public interface PreparedStatementCallback<T> {
    T doInPreparedStatement(PreparedStatement ps) throws SQLException;
}
