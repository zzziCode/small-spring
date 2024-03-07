package com.zzzi.springframework.beans.factory.support;

import cn.hutool.core.util.StrUtil;
import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.DisposableBean;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;

import java.lang.reflect.Method;

/**
 * @author zzzi
 * @date 2023/11/4 14:03
 * 在这里进行统一的destroy方法实现，并且将不同类型的销毁类型进行统一封装
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

    /**@author zzzi
     * @date 2024/3/7 14:12
     * 这个方法要使用bean去判断当前是实现接口还是使用xml配置从而执行销毁逻辑
     * 而原型bean都没有保存到ioc容器中，自然无法获取到原型bean
     * 也就是这里的操作无法执行
     */
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
