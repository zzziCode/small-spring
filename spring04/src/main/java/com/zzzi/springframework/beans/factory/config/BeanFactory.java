package com.zzzi.springframework.beans.factory.config;

/**
 * @author zzzi
 * @date 2023/10/31 19:21
 * 在这里提供获取bean对象的接口
 */
public interface BeanFactory {
    Object getBean(String beanName);

    Object getBean(String beanName, Object... args);
}
