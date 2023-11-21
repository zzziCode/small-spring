package com.zzzi.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zzzi
 * @date 2023/11/21 16:06
 * 在这里对结果集进行封装
 * 针对结果集中的每一行都调用实现了RowMapper的单行结果处理器进行封装
 */
public class RowMapperResultSetExtractor<T> implements ResultSetExtractor<List<T>> {
    //单行结果处理器
    private final RowMapper<T> rowMapper;
    private final int rowsExpected;

    public RowMapperResultSetExtractor(RowMapper<T> rowMapper, int rowsExpected) {
        this.rowMapper = rowMapper;
        this.rowsExpected = rowsExpected;
    }

    public RowMapperResultSetExtractor(RowMapper<T> rowMapper) {
        this(rowMapper, 0);
    }

    /**
     * @author zzzi
     * @date 2023/11/21 16:08
     * 内部将结果集的每一行都使用单行结果处理器进行封装
     */
    @Override
    public List<T> extractData(ResultSet resultSet) throws SQLException {
        List<T> result = this.rowsExpected > 0 ? new ArrayList<>(this.rowsExpected) : new ArrayList<>();
        int rowNum = 0;
        while (resultSet.next()) {
            result.add((this.rowMapper.mapRow(resultSet, rowNum++)));
        }
        return result;
    }
}
