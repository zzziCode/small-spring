package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.beans.factory.config.BeanDefinition;

/**
 * @author zzzi
 * @date 2023/11/1 14:24
 * 在这里提供保存bean注册信息的接口
 * 并且还提供了一些辅助方法，包括获取bean的定义
 * 判断bean的注册信息是否存在
 * 获取到所有已注册的bean的名称
 */
public interface BeanDefinitionRegistry {

    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);

    BeanDefinition getBeanDefinition(String beanName);

    boolean containsBeanDefinition(String beanName);

    String[] getBeanDefinitionNames();
}
