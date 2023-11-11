package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.core.io.DefaultResourceLoader;
import com.zzzi.springframework.core.io.ResourceLoader;

/**
 * @author zzzi
 * @date 2023/11/4 14:51
 * 在这里完成资源加载器的初始化，使得业务类只用关心业务的执行
 * 也就是资源如何加载，初始化在这里完成
 */
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader {
    private final BeanDefinitionRegistry registry;
    private ResourceLoader resourceLoader;

    public AbstractBeanDefinitionReader(BeanDefinitionRegistry registry) {
        this(registry, new DefaultResourceLoader());
    }

    public AbstractBeanDefinitionReader(BeanDefinitionRegistry registry, ResourceLoader resourceLoader) {
        this.registry = registry;
        this.resourceLoader = resourceLoader == null ? new DefaultResourceLoader() : resourceLoader;
    }

    @Override
    public BeanDefinitionRegistry getRegistry() {
        return registry;
    }

    @Override
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }
}
