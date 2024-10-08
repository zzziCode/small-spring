package com.zzzi.springframework.beans.factory.config;

import com.zzzi.springframework.beans.BeansException;

/**
 * @author zzzi
 * @date 2023/11/4 13:32
 * 实现这个接口，实现里面的两个方法就完成了实例化后修改逻辑的实现
 */
public interface BeanPostProcessor {
    //applicationContext资源注入，实例化后的修改逻辑
    Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;

    //尝试AOP
    Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;

}
