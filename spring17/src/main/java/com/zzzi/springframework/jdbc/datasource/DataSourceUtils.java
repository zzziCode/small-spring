package com.zzzi.springframework.jdbc.datasource;

import com.zzzi.springframework.jdbc.CannotGetJdbcConnectionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author zzzi
 * @date 2023/11/21 16:19
 * 在这里完成对数据库连接的管理，获取，释放，关闭等操作
 * 这个类将数据库连接的内容进行了整合，从而在JdbcTemplate中调用
 */
public abstract class DataSourceUtils {

    public static Connection getConnection(DataSource dataSource) {
        try {
            return doGetConnection(dataSource);
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", e);
        }
    }

    public static Connection doGetConnection(DataSource dataSource) throws SQLException {
        Connection connection = fetchConnection(dataSource);
        ConnectionHolder holderToUse = new ConnectionHolder(connection);

        return connection;
    }

    private static Connection fetchConnection(DataSource dataSource) throws SQLException {
        Connection connection = dataSource.getConnection();
        if (connection == null)
            throw new IllegalArgumentException("DataSource return null from getConnection():" + dataSource);
        return connection;
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        try {
            doReleaseConnection(connection, dataSource);
        } catch (SQLException ex) {
//            logger.debug("Could not close JDBC Connection", ex);
        } catch (Throwable ex) {
//            logger.debug("Unexpected exception on closing JDBC Connection", ex);
        }
    }

    public static void doReleaseConnection(Connection connection, DataSource dataSource) throws SQLException {
        connection.close();
    }
}
