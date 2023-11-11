package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.FactoryBean;
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

/**
 * @author zzzi
 * @date 2023/11/7 9:28
 * 将继承的类修改成FactoryBeanRegistrySupport，目的是使用其中实现的方法
 * 可以获取到真正的bean对象
 */
public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements ConfigurableBeanFactory {
    //保存实例化后修改的逻辑
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();
    /**
     * @author zzzi
     * @date 2023/11/6 15:54
     * 在这里增加一个成员变量，保存类加载器
     */
    private ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Object getBean(String name) throws BeansException {
        return doGetBean(name, null);
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        return doGetBean(name, args);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return (T) getBean(name);
    }

    //将所有的获取操作都集中到doGetBean中
    protected <T> T doGetBean(final String beanName, final Object[] args) {
        Object bean = getSingleton(beanName);
        //缓存中有就直接返回真正的bean
        if (bean != null)
            return (T) getObjectForBeanInstance(bean,beanName);
        //缓存中没有就先创建再返回真正的bean
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        Object newBean = createBean(beanName, beanDefinition, args);
        return (T) getObjectForBeanInstance(newBean,beanName);
    }

    /**
     * @author zzzi
     * @date 2023/11/7 9:43
     * 新增一个对获取到的bean进一步处理的方法，防止当前取到的是外壳bean
     */
    protected Object getObjectForBeanInstance(Object beanInstance, String beanName) {
        //1. 普通的bean直接返回
        if(!(beanInstance instanceof FactoryBean)){
            return beanInstance;
        }
        //2. 外壳bean需要得到内部真正的bean对象
        Object bean = getCachedObjectForFactoryBean(beanName);
        //3. 没获取到要么是第一次获取，要么不是单例bean
        //需要创建bean对象
        if(bean==null){
            //转型之后便于调用内部的方法
            FactoryBean<?> factoryBean= (FactoryBean<?>) beanInstance;
            bean = getObjectFromFactoryBean(factoryBean, beanName);
        }
        //4. 返回结果
        return bean;
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
