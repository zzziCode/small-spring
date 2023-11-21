package com.zzzi.springframework.jdbc.support;

import com.zzzi.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;

/**
 * @author zzzi
 * @date 2023/11/21 16:52
 * 在这里设置数据源
 */
public class JdbcAccessor implements InitializingBean {
    private DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected DataSource obtainDatasource() {
        return getDataSource();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (getDataSource() == null) {
            throw new IllegalArgumentException("Property 'datasource' is required");
        }
    }
}
