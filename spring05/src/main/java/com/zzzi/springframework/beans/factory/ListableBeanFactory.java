package com.zzzi.springframework.beans.factory;

import com.zzzi.springframework.beans.BeansException;

import java.util.Map;

/**
 * @author zzzi
 * @date 2023/11/1 15:04
 * 新增两个获取bean对象的方法
 * 按照类型获取bean对象
 * 获取所有已注册的bean对象的名称
 */
public interface ListableBeanFactory {
    <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;
    String[] getBeanDefinitionNames();
}
