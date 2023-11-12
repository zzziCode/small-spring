package com.zzzi.springframework.beans.factory;

/**
 * @author zzzi
 * @date 2023/11/7 9:25
 * 实现这个接口的类，内部在getObject方法中定义真正bean的创建方式
 */
public interface FactoryBean<T> {
    T getObject() throws Exception;

    Class<?> getObjectType();

    boolean isSingleton();
}
