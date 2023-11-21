package com.zzzi.springframework.jdbc;

/**
 * @author zzzi
 * @date 2023/11/21 15:38
 * 自定义的异常类，当结果集中出现不正确的列的数量时报错
 */
public class IncorrectResultSetColumnCountException extends RuntimeException {
    private final int expectedCount;
    private final int actualCount;


    public IncorrectResultSetColumnCountException(int expectedCount, int actualCount) {
        super("Incorrect column count: expected " + expectedCount + ", actual " + actualCount);
        this.expectedCount = expectedCount;
        this.actualCount = actualCount;
    }
}
