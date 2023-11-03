package com.zzzi.springframework.beans.factory.config;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.BeanFactory;

/**
 * @author zzzi
 * @date 2023/11/3 12:19
 * 在这里定义两个待实现的方法，用来实现实例化后修改bean对象
 */
public interface AutowireCapableBeanFactory extends BeanFactory {
    Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) throws BeansException;

    Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) throws BeansException;


}
