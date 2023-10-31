package com.zzzi.springframework.beans.factory.config;

/**@author zzzi
 * @date 2023/10/31 19:17
 * 提供一个获取单例bean的接口
 */
public interface SingletonBeanRegistry {
    Object getSingleton(String beanName);
}
