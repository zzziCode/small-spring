package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.FactoryBean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zzzi
 * @date 2023/11/7 9:28
 * 在这里从实现FactoryBean接口的类中获取到真正的bean
 */
public class FactoryBeanRegistrySupport extends DefaultSingletonBeanRegistry {
    //在这里保存从getObject方法中获取到的单例对象，保存到缓存中
    private final Map<String, Object> factoryBeanObjectCache = new HashMap<>();

    /**
     * @author zzzi
     * @date 2023/11/7 9:31
     * 尝试从缓存中拿到bean
     */
    protected Object getCachedObjectForFactoryBean(String beanName) {
        Object bean = factoryBeanObjectCache.get(beanName);
        //缓存中没有获取到直接返回null，获取到了才返回真正的bean
        return (bean != NULL_OBJECT ? bean : null);
    }

    /**
     * @author zzzi
     * @date 2023/11/7 9:36
     * 在这里获取真正的bean对象，并根据是否是单例模式决定是否保存到缓存中
     */
    protected Object getObjectFromFactoryBean(FactoryBean factoryBean, String beanName) {
        //1. 创建真正的bean对象
        Object bean = doGetObjectFromFactoryBean(factoryBean, beanName);
        //2. factoryBean是单例模式还需要保存到缓存中，因为他是单例，内部的getObject得到的bean需要可以复用
        if (factoryBean.isSingleton()) {
            factoryBeanObjectCache.put(beanName, bean);
        }
        //3. 返回结果
        return bean;
    }

    private Object doGetObjectFromFactoryBean(FactoryBean factoryBean, String beanName) {
        try {
            return factoryBean.getObject();
        } catch (Exception e) {
            throw new BeansException("FactoryBean threw exception on object[" + beanName + "] creation", e);
        }
    }
}
