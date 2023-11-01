package com.zzzi.springframework.beans.factory;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.config.AutowireCapableBeanFactory;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.ConfigurableBeanFactory;

public interface ConfigurableListableBeanFactory extends ConfigurableBeanFactory,ListableBeanFactory, AutowireCapableBeanFactory {
    BeanDefinition getBeanDefinition(String beanName) throws BeansException;
}
