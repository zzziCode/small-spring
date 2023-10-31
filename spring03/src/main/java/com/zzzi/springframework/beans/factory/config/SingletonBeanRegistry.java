package com.zzzi.springframework.beans.factory.config;

/**
 * @author zzzi
 * @date 2023/10/31 13:22
 * 在这里提供获取单例bean的接口
 */
public interface SingletonBeanRegistry {
    Object getSingleton(String beanName);
}
