package com.zzzi.springframework.beans.factory;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.config.AutowireCapableBeanFactory;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * @author zzzi
 * @date 2023/11/3 12:27
 * 增加一个实例化全部bean对象的方法
 */
public interface ConfigurableListableBeanFactory extends ConfigurableBeanFactory, ListableBeanFactory, AutowireCapableBeanFactory {
    BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    void preInstantiateSingletons() throws BeansException;
}
