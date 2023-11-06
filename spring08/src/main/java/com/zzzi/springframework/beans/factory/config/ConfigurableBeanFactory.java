package com.zzzi.springframework.beans.factory.config;

import com.zzzi.springframework.beans.factory.HierarchicalBeanFactory;

/**@author zzzi
 * @date 2023/11/4 13:29
 * 在这里保存spring中的配置信息，并定义一个待实现的保存实例化后修改策略的方法
 */
public interface ConfigurableBeanFactory extends SingletonBeanRegistry, HierarchicalBeanFactory {
    String SCOPE_SINGLETON = "singleton";

    String SCOPE_PROTOTYPE = "prototype";
    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);
}
