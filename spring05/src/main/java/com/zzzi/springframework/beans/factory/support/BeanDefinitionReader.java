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
 * 外部只要调用了这几个loadBeanDefinition方法就可以完成对xml文件的加载工作
 * 并且将加载到的BeanDefinition保存到对应的beanFactory中
 */
public interface BeanDefinitionReader {
    BeanDefinitionRegistry getRegistry();

    ResourceLoader getResourceLoader();

    //提供三种资源利用的方式
    void loadBeanDefinitions(Resource resource) throws BeansException;

    void loadBeanDefinitions(Resource... resources) throws BeansException;

    void loadBeanDefinitions(String location) throws BeansException;
}
