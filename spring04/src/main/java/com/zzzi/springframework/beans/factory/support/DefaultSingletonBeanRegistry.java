package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.beans.factory.config.SingletonBeanRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zzzi
 * @date 2023/10/31 19:19
 * 在这里实现单例bean的获取和保存
 * 将其保存到一个容器中
 */
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {
    //保存所有的单例bean
    Map<String, Object> singletonObjects = new HashMap<>();

    //获取指定的bean
    @Override
    public Object getSingleton(String beanName) {
        return singletonObjects.get(beanName);
    }

    //将创建好的bean保存到容器中
    public void addSingleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName, singletonObject);
    }


}
