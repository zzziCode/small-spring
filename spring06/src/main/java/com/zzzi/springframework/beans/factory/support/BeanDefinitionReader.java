package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.core.io.Resource;
import com.zzzi.springframework.core.io.ResourceLoader;

/**
 * @author zzzi
 * @date 2023/11/1 14:50
 * 在这里提供资源利用的接口
 * 首先通过资源获取得到资源的输入流
 * 然后从资源的输入流中读取配置信息到bean的注册表中
 */
public interface BeanDefinitionReader {
    BeanDefinitionRegistry getRegistry();

    ResourceLoader getResourceLoader();

    //提供三种资源利用的方式
    void loadBeanDefinitions(Resource resource) throws BeansException;

    void loadBeanDefinitions(Resource... resources) throws BeansException;

    void loadBeanDefinitions(String location) throws BeansException;
    void loadBeanDefinitions(String... locations) throws BeansException;
}
