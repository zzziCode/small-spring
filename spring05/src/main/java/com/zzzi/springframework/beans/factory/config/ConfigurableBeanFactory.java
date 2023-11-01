package com.zzzi.springframework.beans.factory.config;

import com.zzzi.springframework.beans.factory.HierarchicalBeanFactory;
import com.zzzi.springframework.beans.factory.ListableBeanFactory;

/**@author zzzi
 * @date 2023/11/1 15:09
 * 在这里定义了一些spring中的配置信息
 */
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {
    String SCOPE_SINGLETON = "singleton";

    String SCOPE_PROTOTYPE = "prototype";
}
