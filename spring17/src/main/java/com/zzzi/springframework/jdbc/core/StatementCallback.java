package com.zzzi.springframework.jdbc.core;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author zzzi
 * @date 2023/11/21 16:45
 * 实现这个接口的类在内部需要执行SQL语句，并且对得到的结果进行封装
 */
public interface StatementCallback<T> {
    T doInStatement(Statement statement) throws SQLException;
}
