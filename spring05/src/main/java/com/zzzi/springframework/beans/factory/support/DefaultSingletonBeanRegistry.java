package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.beans.factory.config.SingletonBeanRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zzzi
 * @date 2023/11/1 13:57
 * 在这里实现单例bean的获取和添加
 * 单例bean对象被保存到一个容器中
 */
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {
    //单例bean对象的保存地
    Map<String, Object> singletonObjects = new HashMap<>();

    //获取单例bean对象
    @Override
    public Object getSingleton(String beanName) {
        return singletonObjects.get(beanName);
    }
    //保存单例bean对象
    public void addSingleton(String beanName,Object singletonBean){
        singletonObjects.put(beanName,singletonBean);
    }

}
