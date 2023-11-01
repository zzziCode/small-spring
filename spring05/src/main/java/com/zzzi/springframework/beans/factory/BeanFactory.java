package com.zzzi.springframework.beans.factory;

import com.zzzi.springframework.beans.BeansException;

/**
 * @author zzzi
 * @date 2023/11/1 13:48
 * 在这里定义对外获取bean对象的接口
 * 主要分为按名称获取，
 * 按名称和参数列表获取
 * 按名称和类型获取
 */
public interface BeanFactory {
    Object getBean(String beanName) throws BeansException;

    Object getBean(String beanName, Object... args) throws BeansException;

    <T> T getBean(String beanName, Class<T> requireType) throws BeansException;
}
