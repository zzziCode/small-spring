package com.zzzi.springframework.aop.framework;

/**
 * @author zzzi
 * @date 2023/11/11 16:11
 * 在这里统一规定创建代理对象的不同策略都要实现的方法
 */
public interface AopProxy {
    Object getProxy();
}
