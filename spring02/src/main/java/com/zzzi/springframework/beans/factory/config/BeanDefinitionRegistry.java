package com.zzzi.springframework.beans.factory.config;


import com.zzzi.springframework.beans.factory.config.BeanDefinition;

/**@author zzzi
 * @date 2023/10/30 12:37
 * 提供一个模板，给保存类信息的容器中加入一个
 * bean类信息与姓名的映射关系
 */
public interface BeanDefinitionRegistry {

    void registerBeanDefinition(String name,BeanDefinition beanDefinition);
}
