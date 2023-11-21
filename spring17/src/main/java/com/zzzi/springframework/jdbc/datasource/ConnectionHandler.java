package com.zzzi.springframework.jdbc.datasource;

import java.sql.Connection;

/**
 * @author zzzi
 * @date 2023/11/21 16:11
 * 实现这个接口的类代表内部保存了一个数据库的连接
 */
public interface ConnectionHandler {
    Connection getConnection();

    default void releaseConnection(Connection connection) {

    }
}
