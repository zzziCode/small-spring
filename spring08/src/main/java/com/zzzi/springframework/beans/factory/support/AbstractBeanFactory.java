package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.BeanPostProcessor;
import com.zzzi.springframework.beans.factory.config.ConfigurableBeanFactory;
import com.zzzi.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zzzi
 * @date 2023/11/4 13:56
 * 在这里保存所有实例化后修改的逻辑
 */
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements ConfigurableBeanFactory {
    //保存实例化后修改的逻辑
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();
    /**@author zzzi
     * @date 2023/11/6 15:54
     * 在这里增加一个成员变量，保存类加载器
     */
    private ClassLoader classLoader= ClassUtils.getDefaultClassLoader();

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Object getBean(String name) throws BeansException {
        return doGetBean(name,null);
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        return doGetBean(name,args);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return (T) getBean(name);
    }

    //将所有的获取操作都集中到doGetBean中
    protected <T> T doGetBean(final String beanName, final Object[] args) {
        Object bean = getSingleton(beanName);
        if (bean != null)
            return (T) bean;
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        return (T) createBean(beanName, beanDefinition, args);
    }

    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    protected abstract Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException;

    public List<BeanPostProcessor> getBeanPostProcessors() {
        return beanPostProcessors;
    }

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        beanPostProcessors.remove(beanPostProcessor);
        beanPostProcessors.add(beanPostProcessor);
    }
}
