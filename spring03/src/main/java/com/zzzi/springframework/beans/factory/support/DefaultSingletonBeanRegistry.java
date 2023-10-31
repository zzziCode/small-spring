package com.zzzi.springframework.beans.factory.support;


import com.zzzi.springframework.beans.factory.config.SingletonBeanRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zzzi
 * @date 2023/10/31 13:23
 * 在这里实现如何获取单例的bean对象
 */
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {
    //在这里保存一个容器，存储bean实例化后的对象及其名称之间的关系
    private Map<String, Object> singletonObjects = new HashMap<>();

    @Override
    public Object getSingleton(String beanName) {
        return singletonObjects.get(beanName);
    }

    /**
     * @author zzzi
     * @date 2023/10/31 13:26
     * 对外提供接口，将实例化后的bean对象保存到容器中供外部使用
     */
    public void addSingleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName, singletonObject);
    }
}
