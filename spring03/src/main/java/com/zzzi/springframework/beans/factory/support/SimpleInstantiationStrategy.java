package com.zzzi.springframework.beans.factory.support;


import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.BeansException;
import com.zzzi.springframework.beans.factory.config.InstantiationStrategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author zzzi
 * @date 2023/10/31 13:56
 * 第一种实例化bean对象的策略：利用JDK的反射来获取bean对象
 */
public class SimpleInstantiationStrategy implements InstantiationStrategy {
    /**
     * @author zzzi
     * @date 2023/10/31 13:57
     * 传递的构造函数不为空，直接创建bean对象
     * 传递的构造函数为空，利用无参构造创建bean对象
     */
    @Override
    public Object instantiate(BeanDefinition beanDefinition, String beanName, Constructor ctor, Object[] args) throws BeansException {
        Class beanClass = beanDefinition.getBeanClass();
        try {
            if (ctor != null) {
                //设置访问权限
                ctor.setAccessible(true);
                return ctor.newInstance(args);
            } else {//传递的构造函数为空，此时利用无参构造创建对象
                return beanClass.getDeclaredConstructor().newInstance();
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new BeansException("Failed to instantiate [" + beanClass.getName() + "]", e);
        }
    }
}
