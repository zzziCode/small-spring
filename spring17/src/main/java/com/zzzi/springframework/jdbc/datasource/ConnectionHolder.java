package com.zzzi.springframework.jdbc.datasource;

import cn.hutool.core.lang.Assert;

import java.sql.Connection;

/**
 * @author zzzi
 * @date 2023/11/21 16:13
 * 在内部保存一个ConnectionHandler对象，代表持有数据库连接
 */
public class ConnectionHolder {
    private ConnectionHandler connectionHandler;
    private Connection currentConnection;

    public ConnectionHolder(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public ConnectionHolder(Connection currentConnection) {
        this.currentConnection = currentConnection;
    }

    public ConnectionHolder(ConnectionHandler connectionHandler, Connection currentConnection) {
        this.connectionHandler = connectionHandler;
        this.currentConnection = currentConnection;
    }

    protected ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    protected boolean hasConnection() {
        return connectionHandler != null;
    }

    protected void setConnection(Connection connection) {
        if (currentConnection != null) {
            if (connectionHandler != null) {
                connectionHandler.releaseConnection((currentConnection));
            }
            currentConnection = null;
        }
        if (connection != null) {
            connectionHandler = new SimpleConnectionHandler(connection);
        } else {
            connectionHandler = null;
        }
    }

    protected Connection getConnection() {
        Assert.notNull(this.connectionHandler, "Active connection is required.");
        if (currentConnection == null) {
            currentConnection = connectionHandler.getConnection();
        }
        return currentConnection;
    }
}

