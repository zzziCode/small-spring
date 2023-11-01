package com.zzzi.springframework.beans.factory.config;
/**@author zzzi
 * @date 2023/11/1 13:46
 * 在这里提供单例bean对象的获取接口
 * 利用名称获取bean对象
 */
public interface SingletonBeanRegistry {
    Object getSingleton(String beanName);
}
