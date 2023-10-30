package com.zzzi.springframework.beans.factory.config;


/**@author zzzi
 * @date 2023/10/30 12:39
 * 对外提供一个获取实例化bean的模板
 */
public interface BeanFactory {
    //尝试获取到实例化单例模式的bean对象,可能抛出异常
    Object getBean(String name) throws BeansException;
}
