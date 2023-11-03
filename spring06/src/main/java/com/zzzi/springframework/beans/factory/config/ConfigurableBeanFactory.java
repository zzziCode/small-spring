package com.zzzi.springframework.beans.factory.config;

import com.zzzi.springframework.beans.factory.HierarchicalBeanFactory;

/**
 * @author zzzi
 * @date 2023/11/1 15:09
 * 在这里定义了一些spring中的配置信息
 * <p>
 * 新增一个保存实例化后修改策略的方法
 */
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {
    String SCOPE_SINGLETON = "singleton";

    String SCOPE_PROTOTYPE = "prototype";

    /**@author zzzi
     * @date 2023/11/3 12:21
     * 新增的方法，用于保存实例化后的修改策略到一个容器中
     */
    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);
}
