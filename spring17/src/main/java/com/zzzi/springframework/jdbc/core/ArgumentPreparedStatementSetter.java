package com.zzzi.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author zzzi
 * @date 2023/11/21 16:50
 * 在这里实现给PreparedStatement进行参数设置的具体逻辑
 */
public class ArgumentPreparedStatementSetter implements PreparedStatementSetter {
    private final Object[] args;

    public ArgumentPreparedStatementSetter(Object[] args) {
        this.args = args;
    }

    //在这里设置参数
    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        if (args != null) {
            for (int i = 1; i <= args.length; i++) {
                ps.setObject(i, args[i - 1]);
            }
        }
    }
}
