package com.zzzi.springframework.jdbc.core;

import java.util.List;
import java.util.Map;

/**
 * @author zzzi
 * @date 2023/11/21 16:55
 * 规定JdbcTemplate中有哪些方法
 * 只提供结构，并不提供实现，具体的实现在JdbcTemplate中
 * 将查询的方法根据返回值分为四类：
 * 1. query
 * 2. queryForList
 * 3. queryForObject
 * 4. queryForMap
 */
public interface JdbcOperations {


    <T> T execute(StatementCallback<T> action);

    void execute(String sql);

    //---------------------------------------------------------------------
    // query
    //---------------------------------------------------------------------

    <T> T query(String sql, ResultSetExtractor<T> res);

    <T> T query(String sql, Object[] args, ResultSetExtractor<T> rse);

    <T> List<T> query(String sql, RowMapper<T> rowMapper);

    <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper);
    /**@author zzzi
     * @date 2023/11/21 19:33
     * 底层被调用的方法
     */
    <T> T query(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse);

    //---------------------------------------------------------------------
    // queryForList，调用
    //---------------------------------------------------------------------

    List<Map<String, Object>> queryForList(String sql);

    <T> List<T> queryForList(String sql, Class<T> elementType);

    <T> List<T> queryForList(String sql, Class<T> elementType, Object... args);

    List<Map<String, Object>> queryForList(String sql, Object... args);

    //---------------------------------------------------------------------
    // queryForObject
    //---------------------------------------------------------------------

    /**@author zzzi
     * @date 2023/11/21 19:34
     * 从List中取出Object的对象返回
     */
    <T> T queryForObject(String sql, RowMapper<T> rowMapper);

    <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper);

    <T> T queryForObject(String sql, Class<T> requiredType);

    //---------------------------------------------------------------------
    // queryForMap
    //---------------------------------------------------------------------

    Map<String, Object> queryForMap(String sql);

    Map<String, Object> queryForMap(String sql, Object... args);
}
