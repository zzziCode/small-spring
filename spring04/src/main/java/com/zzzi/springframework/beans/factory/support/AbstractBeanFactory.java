package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.BeanFactory;
import com.zzzi.springframework.beans.factory.config.BeansException;

/**
 * @author zzzi
 * @date 2023/10/31 19:22
 * 在这里实现获取bean的方法，并且提供一些抽象方法
 * 包括获取bean的注册信息，创建单例bean
 */
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory {

    @Override
    public Object getBean(String beanName) {
        return doGetBean(beanName, null);
    }

    @Override
    public Object getBean(String beanName, Object... args) {
        return doGetBean(beanName, args);
    }

    //为了统一getBean的操作，将他们的操作抽取出来
    public Object doGetBean(String beanName, Object[] args) {
        //尝试获取单例的bean
        Object bean = getSingleton(beanName);
        if (bean != null)
            return bean;

        //没获取到bean，创建一个bean对象
        //先获取bean的注册信息
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        //创建一个bean
        return createBean(beanName,beanDefinition,args);

    }

    protected abstract Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException;

    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

}
