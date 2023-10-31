package com.zzzi.springframework.beans.factory.config;
/**@author zzzi
 * @date 2023/10/31 14:33
 * 在这里提供保存bean注册信息的接口
 */
public interface BeanDefinitionRegistry {
    void registerBeanDefinition(String beanName,BeanDefinition beanDefinition);
}
