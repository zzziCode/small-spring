package com.zzzi.springframework.beans.factory;

/**
 * @author zzzi
 * @date 2023/11/7 9:25
 * 实现这个接口的类，内部在getObject方法中定义真正bean的创建方式
 * 这是一种新的bean的实例化方式，这种方式可以将bean的实例化过程更多的交给程序员控制
 */
public interface FactoryBean<T> {
    T getObject() throws Exception;

    Class<?> getObjectType();

    boolean isSingleton();
}
