package com.zzzi.springframework.jdbc.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.zzzi.springframework.jdbc.UncategorizedSQLException;
import com.zzzi.springframework.jdbc.core.*;
import com.zzzi.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * @author zzzi
 * @date 2023/11/21 16:58
 * 整个jdbc模块的核心，将jdbc模块中的所有功能都集成到这一个类中
 * 外部用户调用这个类就可以使用jdbc模块中的功能从而执行sql语句得到封装后的结果
 */
public class JdbcTemplate extends JdbcAccessor implements JdbcOperations {
    //默认下面三个变量都是不进行限制
    //控制每次查询返回的记录条数
    private int fetchSize = -1;
    //设置返回的记录最大数
    private int maxRows = -1;
    //设置超时时间
    private int queryTimeout = -1;

    /**
     * @author zzzi
     * @date 2023/11/21 18:56
     * 使用Statement的SQL执行器执行sql语句
     * 第一个核心方法
     */
    private <T> T execute(StatementCallback<T> action, boolean closeResources) {
        Connection con = DataSourceUtils.getConnection(obtainDatasource());

        Statement stmt = null;
        try {
            stmt = con.createStatement();
            //准备这是stmt执行器的参数
            applyStatementSettings(stmt);
            /**@author zzzi
             * @date 2023/11/20 16:14
             * 这个方法中不仅执行了SQL语句，而且将执行SQL语句得到的结果集还进行了封装
             */
            return action.doInStatement(stmt);

        } catch (SQLException e) {
            String sql = getSql(action);
            JdbcUtils.closeStatement(stmt);
            stmt = null;
            throw translateException("ConnectionCallback", sql, e);
        } finally {
            if (closeResources) {
                JdbcUtils.closeStatement(stmt);
            }
        }
    }

    /**
     * @author zzzi
     * @date 2023/11/21 18:56
     * 使用PreparedStatement的SQL执行器执行SQL语句
     * 第二个核心方法
     */
    private <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action, boolean closeResources) {

        Assert.notNull(psc, "PreparedStatementCreator must not be null");
        Assert.notNull(action, "Callback object must not be null");


        Connection con = DataSourceUtils.getConnection(obtainDatasource());
        PreparedStatement ps = null;
        try {
            ps = psc.createPreparedStatement(con);
            //准备设置ps执行器的参数
            applyStatementSettings(ps);
            T result = action.doInPreparedStatement(ps);
            return result;
        } catch (SQLException ex) {

            String sql = getSql(psc);
            psc = null;
            JdbcUtils.closeStatement(ps);
            ps = null;
            DataSourceUtils.releaseConnection(con, getDataSource());
            con = null;
            throw translateException("PreparedStatementCallback", sql, ex);
        } finally {
            if (closeResources) {

                JdbcUtils.closeStatement(ps);
                DataSourceUtils.releaseConnection(con, getDataSource());
            }
        }
    }

    /**
     * @author zzzi
     * @date 2023/11/21 18:57
     * 使用PreparedStatement的SQL执行器执行SQL语句
     * 第三个核心方法
     */
    public <T> T query(PreparedStatementCreator psc, final PreparedStatementSetter pss, final ResultSetExtractor<T> rse) {

        Assert.notNull(rse, "ResultSetExtractor must not be null");

        return execute(psc, new PreparedStatementCallback<T>() {
            @Override
            public T doInPreparedStatement(PreparedStatement ps) throws SQLException {
                ResultSet rs = null;
                try {
                    if (pss != null) {
                        pss.setValues(ps);
                    }
                    rs = ps.executeQuery();
                    //将结果封装成List
                    return rse.extractData(rs);
                } finally {
                    JdbcUtils.closeResultSet(rs);
                }
            }
        }, true);
    }

    /**@author zzzi
     * @date 2023/11/21 19:38
     * 这种就是将查询得到的被封装在List中的一条记录遍历出来
     * 第四个核心方法
     */
    @Override
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) {
        List<T> results = query(sql, rowMapper);

        if (CollUtil.isEmpty(results)) {
            throw new RuntimeException("Incorrect result size: expected 1, actual 0");
        }
        if (results.size() > 1) {
            throw new RuntimeException("Incorrect result size: expected 1, actual " + results.size());
        }
        return results.iterator().next();
    }
    /**@author zzzi
     * @date 2023/11/21 19:38
     * 第五个核心方法
     */
    @Override
    public <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) {
        List<T> results = query(sql, args, new RowMapperResultSetExtractor<>(rowMapper, 1));

        //下面两个判断条件保证查询结果只有一条
        if (CollUtil.isEmpty(results)) {
            throw new RuntimeException("Incorrect result size: expected 1, actual 0");
        }
        //包
        if (results.size() > 1) {
            throw new RuntimeException("Incorrect result size: expected 1, actual " + results.size());
        }
        //将封装在List中的一条记录便利出来返回就是查询Object
        return results.iterator().next();
    }

    /*========================================================分割线===================================================================*/
    /**@author zzzi
     * @date 2023/11/21 19:38
     * 剩下的方法都是调用上面的五个方法从而执行SQL语句，返回执行的结果
     */
    //在SQL执行器执行SQL语句之前，设置这些通用的参数
    //在两种执行器执行的内部调用
    protected void applyStatementSettings(Statement stat) throws SQLException {
        int fetchSize = getFetchSize();
        if (fetchSize != -1) {
            stat.setFetchSize(fetchSize);
        }
        int maxRows = getMaxRows();
        if (maxRows != -1) {
            stat.setMaxRows(maxRows);
        }

    }

    //处理异常的类，在两种执行器执行的内部调用
    protected UncategorizedSQLException translateException(String task, String sql, SQLException ex) {
        return new UncategorizedSQLException(task, sql, ex);
    }

    //提供获取当前正在执行的sql语句的方法
    private static String getSql(Object sqlProvider) {
        if (sqlProvider instanceof SqlProvider) {
            return ((SqlProvider) sqlProvider).getSql();
        } else {
            return null;
        }
    }

    //调用别的execute方法
    @Override
    public <T> T execute(StatementCallback<T> action) {
        return execute(action, true);
    }

    //调用别的execute方法
    @Override
    public void execute(String sql) {

        //执行不需要返回值
        class ExecuteStatementCallback implements StatementCallback<Object>, SqlProvider {

            @Override
            public Object doInStatement(Statement statement) throws SQLException {
                statement.execute(sql);
                return null;
            }

            @Override
            public String getSql() {
                return sql;
            }
        }
        //调用别的execute方法
        execute(new ExecuteStatementCallback(), true);
    }

    //调用别的execute方法
    @Override
    public <T> T query(String sql, ResultSetExtractor<T> res) {
        Assert.notNull(sql, "SQL must not be null");
        Assert.notNull(res, "ResultSetExtractor must be null");

        //查询可以得到返回值
        //没有Callback这个对象的话，就在方法内部新建一个，然后调用别的execute方法
        class QueryStatementCallback implements StatementCallback<T>, SqlProvider {

            @Override
            public String getSql() {
                return sql;
            }

            /**@author zzzi
             * @date 2023/11/20 15:59
             * 在这里真正的执行SQL语句，并进行封装返回
             */
            @Override
            public T doInStatement(Statement statement) throws SQLException {
                ResultSet rs = statement.executeQuery(sql);
                return res.extractData(rs);
            }
        }

        //调用别的execute方法
        return execute(new QueryStatementCallback(), true);
    }

    //调用别的query方法
    @Override
    public <T> T query(String sql, Object[] args, ResultSetExtractor<T> rse) {
        return query(sql, newArgPreparedStatementSetter(args), rse);
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        //传递一个sql语句和一个处理结果集的工具，内部针对结果集的每一行都封装成一个Map
        return result(query(sql, new RowMapperResultSetExtractor<>(rowMapper)));
    }

    //调用别的query方法
    @Override
    public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) {
        return result(query(sql, args, new RowMapperResultSetExtractor<>(rowMapper)));
    }

    @Override
    public <T> T query(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse) {
        return query(new SimplePreparedStatementCreator(sql), pss, rse);
    }


    @Override
    public <T> T queryForObject(String sql, Class<T> requiredType) {
        return queryForObject(sql, getSingleColumnRowMapper(requiredType));
    }

    @Override
    public Map<String, Object> queryForMap(String sql) {
        return result(queryForObject(sql, getColumnMapRowMapper()));
    }

    @Override
    public Map<String, Object> queryForMap(String sql, Object... args) {
        return result(queryForObject(sql, args, getColumnMapRowMapper()));
    }

    //为什么是List中包含了一个Map
    @Override
    public List<Map<String, Object>> queryForList(String sql) {
        //传递一个sql语句以及一个处理结果集中一行数据的工具
        return query(sql, getColumnMapRowMapper());
    }

    @Override
    public <T> List<T> queryForList(String sql, Class<T> elementType) {
        return query(sql, getSingleColumnRowMapper(elementType));
    }

    @Override
    public <T> List<T> queryForList(String sql, Class<T> elementType, Object... args) {
        return query(sql, args, getSingleColumnRowMapper(elementType));
    }

    @Override
    public List<Map<String, Object>> queryForList(String sql, Object... args) {
        return query(sql, args, getColumnMapRowMapper());
    }

    //对结果进行处理
    private static <T> T result(T result) {
        Assert.state(null != result, "No result");
        return result;
    }

    //获得不同的结果集封装工具
    protected RowMapper<Map<String, Object>> getColumnMapRowMapper() {
        return new ColumnMapRowMapper();
    }

    protected <T> RowMapper<T> getSingleColumnRowMapper(Class<T> requiredType) {
        return new SingleColumnRowMapper<>(requiredType);
    }

    /**
     * @author zzzi
     * @date 2023/11/20 19:04
     * 用来设置sql查询时传递的参数
     */
    protected PreparedStatementSetter newArgPreparedStatementSetter(Object[] args) {
        return new ArgumentPreparedStatementSetter(args);
    }

    /**
     * @author zzzi
     * @date 2023/11/20 19:20
     * 内部保存传递的sql语句，用来辅助sql语句的执行
     */
    private static class SimplePreparedStatementCreator implements PreparedStatementCreator, SqlProvider {

        private final String sql;

        public SimplePreparedStatementCreator(String sql) {
            this.sql = sql;
        }


        @Override
        public String getSql() {
            return this.sql;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            return con.prepareStatement(this.sql);
        }
    }

    /**
     * @author zzzi
     * @date 2023/11/21 18:44
     * 利用构造函数初始化数据源
     */
    public JdbcTemplate() {
    }

    /**
     * @author zzzi
     * @date 2023/11/21 18:44
     * 采用构造函数注入的方式，当数据源存在的情况下JdbcTemplate才会创建对象
     */
    public JdbcTemplate(DataSource dataSource) throws Exception {
        setDataSource(dataSource);
        afterPropertiesSet();
    }

    /**
     * @author zzzi
     * @date 2023/11/21 18:43
     * 对三个变量设置get和set方法
     */
    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }
}
