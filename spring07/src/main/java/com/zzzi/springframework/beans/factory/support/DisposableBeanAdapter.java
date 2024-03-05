package com.zzzi.springframework.beans.factory.support;

import cn.hutool.core.util.StrUtil;
import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.DisposableBean;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author zzzi
 * @date 2023/11/4 14:03
 * 在这里进行统一的destroy方法实现，并且将不同类型的销毁类型进行统一封装
 * 这符合适配器设计模式，将实现了接口的bean的销毁逻辑以及xml配置的销毁逻辑统一保存
 */
public class DisposableBeanAdapter implements DisposableBean {
    private final Object bean;
    private final String beanName;
    private String destroyMethodName;

    public DisposableBeanAdapter(Object bean, String beanName, BeanDefinition beanDefinition) {
        this.bean = bean;
        this.beanName = beanName;
        this.destroyMethodName = beanDefinition.getDestroyMethodName();
    }

    @Override
    public void destroy() throws Exception {
        //1. 实现接口自定义销毁逻辑
        if (bean instanceof DisposableBean) {
            ((DisposableBean) bean).destroy();
        }
        //2. xml配置文件实现销毁逻辑
        if (StrUtil.isNotEmpty(destroyMethodName) && !(bean instanceof DisposableBean && "destroy".equals(this.destroyMethodName))) {
            Class<?> beanClass = bean.getClass();
            Method destroyMethod = beanClass.getMethod(destroyMethodName);
            if (null == destroyMethod) {
                throw new BeansException("Couldn't find a destroy method named '" + destroyMethodName + "' on bean with name '" + beanName + "'");
            }
            destroyMethod.invoke(bean);
        }
    }
}
