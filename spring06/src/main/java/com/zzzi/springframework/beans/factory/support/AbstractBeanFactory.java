package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.BeanPostProcessor;
import com.zzzi.springframework.beans.factory.config.ConfigurableBeanFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zzzi
 * @date 2023/11/1 14:00
 * 在这里实现对外获取bean对象的getBean方法
 */
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements ConfigurableBeanFactory {
    /**
     * @author zzzi
     * @date 2023/11/3 12:25
     * 在这里新增一个容器，保存所有的实例化策略
     */
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    @Override
    public Object getBean(String beanName) throws BeansException {
        return doGetBean(beanName, null);
    }

    @Override
    public Object getBean(String beanName, Object... args) throws BeansException {
        return doGetBean(beanName, args);
    }

    @Override
    public <T> T getBean(String beanName, Class<T> requireType) throws BeansException {
        return (T) getBean(beanName);
    }

    //将所有获取bean对象的操作集中到这个方法中
    protected <T> T doGetBean(final String beanName, final Object[] args) {
        Object bean = getSingleton(beanName);
        //尝试获取已经实例化的bean
        if (bean != null)
            return (T) bean;
        //没有获取到就自己创建
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        return (T) createBean(beanName, beanDefinition, args);
    }

    /**@author zzzi
     * @date 2023/11/3 15:06
     * 在这里新增对保存自定义实例化后修改策略的容器的set和get方法
     */
    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        //先删除再加入
        beanPostProcessors.remove(beanPostProcessor);
        beanPostProcessors.add(beanPostProcessor);
    }
    public List<BeanPostProcessor> getBeanPostProcessors(){
        return beanPostProcessors;
    }

    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    protected abstract Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException;

}
