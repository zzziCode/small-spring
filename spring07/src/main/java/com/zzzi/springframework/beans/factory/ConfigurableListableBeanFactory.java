package com.zzzi.springframework.beans.factory;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.config.AutowireCapableBeanFactory;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * @author zzzi
 * @date 2023/11/4 13:38
 * 在这里增加两个方法
 */
public interface ConfigurableListableBeanFactory extends ConfigurableBeanFactory, AutowireCapableBeanFactory, ListableBeanFactory {
    BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    void preInstantiateSingletons() throws BeansException;
}
