package com.zzzi.springframework.beans.factory;

import com.zzzi.springframework.beans.BeansException;

import java.util.Map;

/**
 * @author zzzi
 * @date 2023/11/4 13:23
 * 在这类进一步给beanFactory增加几个方法
 */
public interface ListableBeanFactory extends BeanFactory {
    <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;

    String[] getBeanDefinitionNames();

}
