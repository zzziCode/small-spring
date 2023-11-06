package com.zzzi.springframework.beans.factory;

import com.zzzi.springframework.beans.BeansException;

/**
 * @author zzzi
 * @date 2023/11/6 15:40
 * 实现这个接口的bean被认为内部需要beanFactory这个容器资源
 * spring会通过这个setBeanFactory接口注入容器资源
 */
public interface BeanFactoryAware extends Aware {
    void setBeanFactory(BeanFactory beanFactory) throws BeansException;
}
