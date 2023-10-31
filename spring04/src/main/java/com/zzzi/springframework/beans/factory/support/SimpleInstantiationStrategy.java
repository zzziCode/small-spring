package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.BeansException;
import com.zzzi.springframework.beans.factory.config.InstantiationStrategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**@author zzzi
 * @date 2023/10/31 19:35
 * 第一种实例化bean对象的策略：利用JDK的反射
 */
public class SimpleInstantiationStrategy implements InstantiationStrategy {
    /**@author zzzi
     * @date 2023/10/31 19:35
     * 如果传递而来的构造函数不为空，直接利用参数和构造函数创建实例对象并返回
     * 如果传递而来的构造函数为空，代表没找到匹配的构造函数，利用无参构造创建对象并返回
     */
    @Override
    public Object instantiate(BeanDefinition beanDefinition, String beanName, Constructor ctor, Object[] args) throws BeansException {
        try {
            if(ctor!=null){//利用有参构造创建bean对象并返回
                return ctor.newInstance(args);
            }else{//利用无参构造创建bean对象并返回
                Class beanClass = beanDefinition.getBeanClass();
                return beanClass.getDeclaredConstructor().newInstance();
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new BeansException("Failed to instantiate [" + beanDefinition.getBeanClass().getName() + "]", e);
        }
    }
}
