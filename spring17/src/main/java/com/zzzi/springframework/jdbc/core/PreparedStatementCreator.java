package com.zzzi.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author zzzi
 * @date 2023/11/21 16:47
 * 实现这个接口的类在内部创建一个预编译的PreparedStatement
 */
public interface PreparedStatementCreator {
    PreparedStatement createPreparedStatement(Connection connection) throws SQLException;
}
