package com.zzzi.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author zzzi
 * @date 2023/11/21 16:48
 * 实现这个接口的类在内部对预编译的PreparedStatement进行参数设置
 */
public interface PreparedStatementSetter {
    void setValues(PreparedStatement ps) throws SQLException;
}
