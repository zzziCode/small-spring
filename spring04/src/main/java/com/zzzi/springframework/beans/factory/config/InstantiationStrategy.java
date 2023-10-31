package com.zzzi.springframework.beans.factory.config;

import java.lang.reflect.Constructor;

/**
 * @author zzzi
 * @date 2023/10/31 19:33
 * 在这里提供实例化的策略
 */
public interface InstantiationStrategy {
    Object instantiate(BeanDefinition beanDefinition, String beanName, Constructor ctor, Object[] args) throws BeansException;
}
