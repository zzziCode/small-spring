package com.zzzi.springframework.beans.factory;

import com.zzzi.springframework.beans.BeansException;

/**
 * @author zzzi
 * @date 2023/11/4 13:12
 * 在这里提供所有获取bean对象的待实现接口
 */
public interface BeanFactory {
    Object getBean(String name) throws BeansException;

    Object getBean(String name, Object... args) throws BeansException;

    <T> T getBean(String name, Class<T> requiredType) throws BeansException;

    /**
     * @author zzzi
     * @date 2023/11/13 16:11
     * 新增一个只用类型获取bean的方法
     */
    <T> T getBean(Class<T> requiredType) throws BeansException;

    /**
     * @author zzzi
     * @date 2023/11/20 10:47
     * 新增一个方法
     */
    boolean containsBean(String name);
}
