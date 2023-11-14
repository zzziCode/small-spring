package com.zzzi.springframework.beans.factory;

import com.zzzi.springframework.beans.BeansException;

/**
 * @author zzzi
 * @date 2023/11/6 15:42
 * 实现这个接口的bean，spring认为其需要beanName这个容器资源
 * 于是会通过set接口注入
 */
public interface BeanNameAware extends Aware {
    void setBeanName(String beanName) throws BeansException;
}
