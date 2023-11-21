package com.zzzi.springframework.jdbc.datasource;

import cn.hutool.core.lang.Assert;

import java.sql.Connection;

/**
 * @author zzzi
 * @date 2023/11/21 16:12
 * 在内部保存一个数据库的连接
 */
public class SimpleConnectionHandler implements ConnectionHandler {
    private final Connection connection;

    public SimpleConnectionHandler(Connection connection) {
        Assert.notNull(connection, "Connection must not be null");
        this.connection = connection;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

}
