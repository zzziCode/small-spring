package com.zzzi.springframework.beans.factory.config;

import com.zzzi.springframework.beans.BeansException;

/**
 * @author zzzi
 * @date 2023/11/3 12:15
 * 实现这个接口就可以自定义一个实例化后的修改策略
 */
public interface BeanPostProcessor {
    Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;

    Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;
}
