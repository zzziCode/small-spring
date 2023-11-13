package com.zzzi.springframework.context;

import com.zzzi.springframework.beans.BeansException;

/**
 * @author zzzi
 * @date 2023/11/4 15:04
 * 在这里定义应用上下文中的核心方法，刷新
 * 另外增加两个方法，一个注册钩子函数，一个关闭函数
 */
public interface ConfigurableApplicationContext extends ApplicationContext {
    void refresh() throws BeansException;

    void registerShutdownHook();

    void close();
}
