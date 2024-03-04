package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.DisposableBean;
import com.zzzi.springframework.beans.factory.ObjectFactory;
import com.zzzi.springframework.beans.factory.config.SingletonBeanRegistry;

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
    //保存所有的销毁逻辑
    Map<String, DisposableBean> disposableBeanMap = new HashMap<>();
    //新增一个常量
    protected static final Object NULL_OBJECT = new Object();


    //保存所有的实例化bean对象，单例池，一级缓存
    Map<String, Object> singletonObjects = new HashMap<>();
    /**
     * @author zzzi
     * @date 2023/11/16 19:23
     * 在这里引入另外两个缓存容器，这样项目中就有三级缓存了
     */
    //二级缓存
    protected final Map<String, Object> earlySingletonObjects = new HashMap<>();
    //三级缓存
    protected final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>();

    /**
     * @author zzzi
     * @date 2023/11/16 19:29
     * 重构这个方法，从原来的直接从一级缓存中拿变成现在的从三级缓存中拿
     */
    @Override
    public Object getSingleton(String beanName) {
        //从一级缓存中拿
        Object singletonObject = singletonObjects.get(beanName);
        if (singletonObject == null) {
            //从二级缓存中拿
            singletonObject = earlySingletonObjects.get(beanName);
            if (singletonObject == null) {
                //从三级缓存中拿
                ObjectFactory<?> objectFactory = singletonFactories.get(beanName);
                if (objectFactory != null) {
                    //调用这个方法会调用bean创建初期保存的lambda表达式中的内容
                    singletonObject = objectFactory.getObject();
                    earlySingletonObjects.put(beanName, singletonObject);
                    singletonFactories.remove(beanName);
                }
            }
        }
        return singletonObject;
    }

    /**
     * @author zzzi
     * @date 2023/11/8 15:22
     * 向单例池中保存一个单例bean对象
     */
    /**
     * @author zzzi
     * @date 2023/11/16 19:34
     * 创建得到的单例bean对象会调用这个方法进行保存
     * 对这个方法进行重构，保存到一级缓存单例池中之后，三级缓存和二级缓存中的内容需要删除
     */
    @Override
    public void registerSingleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName, singletonObject);
        earlySingletonObjects.remove(beanName);
        singletonFactories.remove(beanName);
    }

    public void addSingleton(String beanName, Object singleton) {
        singletonObjects.put(beanName, singleton);
    }

    /**
     * @author zzzi
     * @date 2023/11/16 19:37
     * 新增一个方法，主要是在bean创建初期，得到空bean之后就提前暴露
     * 提前暴露到三级缓存中
     * 这样就可以解决循环依赖
     */
    protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        //三级缓存中不存在才保存
        if (!singletonFactories.containsKey(beanName)) {
            singletonFactories.put(beanName, singletonFactory);
            earlySingletonObjects.remove(beanName);
        }
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
