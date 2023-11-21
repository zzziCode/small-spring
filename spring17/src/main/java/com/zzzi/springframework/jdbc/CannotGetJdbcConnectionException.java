package com.zzzi.springframework.jdbc;

import java.sql.SQLException;
/**@author zzzi
 * @date 2023/11/21 15:36
 * 自定义的异常类，无法获取到数据库的连接时报错
 */
public class CannotGetJdbcConnectionException extends RuntimeException {
    public CannotGetJdbcConnectionException(String message) {
        super(message);
    }

    public CannotGetJdbcConnectionException(String message, SQLException exception) {
        super(message, exception);
    }
}
