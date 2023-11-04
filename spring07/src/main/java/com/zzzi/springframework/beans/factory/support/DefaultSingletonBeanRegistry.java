package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.DisposableBean;
import com.zzzi.springframework.beans.factory.config.SingletonBeanRegistry;
import sun.security.util.Length;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author zzzi
 * @date 2023/11/4 13:40
 * 在这里保存两种结果，第一种是实例化后的bean对象
 * 第二种是销毁逻辑对象
 */
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {
    //保存所有的实例化bean对象
    Map<String, Object> singletonObjects = new HashMap<>();
    //保存所有的销毁逻辑
    Map<String, DisposableBean> disposableBeanMap = new HashMap<>();

    @Override
    public Object getSingleton(String beanName) {
        return singletonObjects.get(beanName);
    }

    public void addSingleton(String beanName, Object singleton) {
        singletonObjects.put(beanName, singleton);
    }

    public void registerDisposableBean(String beanName, DisposableBean disposableBean) {
        disposableBeanMap.put(beanName, disposableBean);
    }

    public void destroySingletons() {
        //拿到所有销毁方法的方法名
        Set<String> keySet = this.disposableBeanMap.keySet();
        Object[] disposableBeanNames = keySet.toArray();

        for (int i = disposableBeanNames.length - 1; i >= 0; i--) {
            Object beanName = disposableBeanNames[i];
            //拿到的是一个销毁对象，里面封装了销毁的执行逻辑
            DisposableBean disposableBean = disposableBeanMap.remove(beanName);
            try {
                disposableBean.destroy();
            } catch (Exception e) {
                throw new BeansException("Destroy method on bean with name '" + beanName + "' throw an exception", e);
            }
        }
    }
}
