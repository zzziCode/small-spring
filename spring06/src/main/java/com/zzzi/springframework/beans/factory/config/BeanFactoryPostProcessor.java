package com.zzzi.springframework.beans.factory.config;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.ConfigurableListableBeanFactory;

/**
 * @author zzzi
 * @date 2023/11/3 12:17
 * 实现这个接口就可以自定义一个实例化前的修改策略
 */
public interface BeanFactoryPostProcessor {
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;
}
