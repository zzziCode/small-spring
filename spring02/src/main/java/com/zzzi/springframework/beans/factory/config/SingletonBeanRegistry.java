package com.zzzi.springframework.beans.factory.config;

/**@author zzzi
 * @date 2023/10/30 12:41
 * 从容器中尝试获取bean的实例化单例模式的对象的模板
 */
public interface SingletonBeanRegistry {

    Object getSingleton(String name);
}
