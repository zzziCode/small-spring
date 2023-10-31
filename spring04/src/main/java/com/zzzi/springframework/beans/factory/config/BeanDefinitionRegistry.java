package com.zzzi.springframework.beans.factory.config;

/**@author zzzi
 * @date 2023/10/31 20:04
 * 在这里提供保存bean注册信息的接口
 * 主要是将所有的bean的注册信息保存到一个容器中
 */
public interface BeanDefinitionRegistry {
    void registerBeanDefinition(String beanName,BeanDefinition beanDefinition);
}
