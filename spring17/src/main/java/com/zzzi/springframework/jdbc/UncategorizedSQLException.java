package com.zzzi.springframework.jdbc;

/**
 * @author zzzi
 * @date 2023/11/21 15:40
 * 自定义的异常类，当出现位置的SQL异常就会出现这个异常
 */
public class UncategorizedSQLException extends RuntimeException {
    public UncategorizedSQLException(String message) {
        super(message);
    }

    public UncategorizedSQLException(String task, String sql, Throwable cause) {
        super(sql, cause);
    }
}
