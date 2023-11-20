package com.zzzi.springframework.beans.factory.config;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.ConfigurableListableBeanFactory;

/**@author zzzi
 * @date 2023/11/4 13:33
 * 实现这个接口中的方法，就可以实现实例化前修改逻辑
 */
public interface BeanFactoryPostProcessor {
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}
