package com.zzzi.springframework.beans.factory.config;


/**
 * @author zzzi
 * @date 2023/10/31 13:27
 * 在这里提供getBean的接口，注意需要提供多种重载版本
 * 其中包括根据参数获取bean对象
 */
public interface BeanFactory {
    //单纯使用名称获取无参bean对象
    Object getBean(String beanName) throws BeansException;

    //使用参数获取带参bean对象、
    Object getBean(String beanName, Object... args) throws BeansException;
}
