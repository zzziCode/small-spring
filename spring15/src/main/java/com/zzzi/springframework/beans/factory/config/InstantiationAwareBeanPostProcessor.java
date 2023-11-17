package com.zzzi.springframework.beans.factory.config;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.PropertyValues;

/**
 * @author zzzi
 * @date 2023/11/11 16:41
 * 在这里新增一个感知接口，继承这个接口的实现类可以被spring所感知
 * 从而可以调用其中新增的方法
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {
    /**
     * @author zzzi
     * @date 2023/11/11 16:43
     * 这是新增的方法，核心的功能都在这个方法中实现：
     * 匹配每一个bean，尝试对当前bean做一个代理
     * 创建代理时调用代理工厂得到代理对象
     * 调用代理对象的方法时会进入invoke方法或者intercept方法中
     * 从而执行方法的匹配
     * 决定执行拦截器中定义的通知和原始方法的执行逻辑还是直接执行原始方法
     */
    Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException;

    /**
     * @author zzzi
     * @date 2023/11/14 13:43
     * 在这里新增一个方法
     */
    boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException;

    /**
     * @author zzzi
     * @date 2023/11/13 15:26
     * 新增一个方法，用来处理注解属性填充
     */
    PropertyValues postProcessPropertyValues(PropertyValues pvs, Object bean, String beanName) throws BeansException;

    /**
     * @author zzzi
     * @date 2023/11/16 19:41
     * 接口中新增一个方法，用来将AOP的创建提前，从而可以提前暴露bean对象，解决循环依赖的问题
     */
    default Object getEarlyBeanReference(Object bean, String beanName) {
        return bean;
    }

}
