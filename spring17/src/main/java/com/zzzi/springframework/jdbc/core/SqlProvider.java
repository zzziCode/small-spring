package com.zzzi.springframework.jdbc.core;

/**
 * @author zzzi
 * @date 2023/11/21 16:52
 * 实现这个接口的类说明可以对外提供当前正在执行的sql是什么
 */
public interface SqlProvider {
    String getSql();
}
