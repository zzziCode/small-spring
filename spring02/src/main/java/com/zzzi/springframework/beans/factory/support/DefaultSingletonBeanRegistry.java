package com.zzzi.springframework.beans.factory.support;


import com.zzzi.springframework.beans.factory.config.SingletonBeanRegistry;

import java.util.HashMap;
import java.util.Map;

/**@author zzzi
 * @date 2023/10/30 12:44
 * 在这里实现获取单例模式的bean对象的方法
 * 这个类继承而来的方法有:
 * 1.getSingleton:单纯用来从容器中获取一个单例模式的bean对象
 */
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {
    //提供一个容器保存实例化后的bean对象和其名称之间的映射
    private final Map<String,Object> singletonObjects =new HashMap<>();
    /**@author zzzi
     * @date 2023/10/30 12:46
     * 实现从保存bean实例化对象的额容器中获取单例对象的方法
     */
    @Override
    public Object getSingleton(String name) {
        return singletonObjects.get(name);
    }

    /**@author zzzi
     * @date 2023/10/30 12:46
     * 根据外部传递来的bean对象,将其保存到容器中
     */
    public void addSingleton(String name,Object singletonObject){
        singletonObjects.put(name,singletonObject);
    }
}
