package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.BeanDefinitionRegistry;
import com.zzzi.springframework.beans.factory.config.BeansException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zzzi
 * @date 2023/10/31 20:05
 * 在这里实现最后的两个未实现的方法
 * 一个是bean注册信息的保存，一个是bean注册信息的获取
 */
//经过不断地继承，在这里已经将项目中所有的方法都实现了，对外使用这个类调用这些已经被实现的方法即可
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements BeanDefinitionRegistry {
    //保存所有bean的注册信息
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    @Override
    protected BeanDefinition getBeanDefinition(String beanName) throws BeansException {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) throw new BeansException("No bean named '" + beanName + "' is defined");
        return beanDefinition;
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanName, beanDefinition);
    }
}
