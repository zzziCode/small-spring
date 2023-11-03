package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.core.io.DefaultResourceLoader;
import com.zzzi.springframework.core.io.ResourceLoader;

/**
 * @author zzzi
 * @date 2023/11/1 14:56
 * 在这里将需要的bean的注册表和资源获取得到的对象拿到
 * 下面的实现类只关注如何资源利用，这些前置工作在这里做了
 */
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader {
    private BeanDefinitionRegistry beanDefinitionRegistry;
    private ResourceLoader resourceLoader;

    public AbstractBeanDefinitionReader(BeanDefinitionRegistry beanDefinitionRegistry) {
        this(beanDefinitionRegistry,new DefaultResourceLoader());
    }

    public AbstractBeanDefinitionReader(BeanDefinitionRegistry beanDefinitionRegistry, ResourceLoader resourceLoader) {
        this.beanDefinitionRegistry = beanDefinitionRegistry;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public BeanDefinitionRegistry getRegistry() {
        return beanDefinitionRegistry;
    }

    @Override
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }
}
