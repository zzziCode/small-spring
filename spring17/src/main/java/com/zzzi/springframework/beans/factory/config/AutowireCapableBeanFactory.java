package com.zzzi.springframework.beans.factory.config;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.BeanFactory;

/**
 * @author zzzi
 * @date 2023/11/4 13:36
 * 在这里面提供实例化后修改的逻辑的接口
 */
public interface AutowireCapableBeanFactory extends BeanFactory {
    Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) throws BeansException;

    Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) throws BeansException;

}
