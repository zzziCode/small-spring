package com.zzzi.springframework.beans.factory.config;

/**
 * @author zzzi
 * @date 2023/11/4 13:27
 * 这里有获取和销毁单例bean对象的待实现方法
 */
public interface SingletonBeanRegistry {
    Object getSingleton(String beanName);

    void registerSingleton(String beanName, Object singletonObject);
}
