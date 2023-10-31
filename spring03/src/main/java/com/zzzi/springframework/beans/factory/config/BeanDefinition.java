package com.zzzi.springframework.beans.factory.config;

/**
 * @author zzzi
 * @date 2023/10/31 13:21
 * 这里保存bean的注册信息，主要是类信息
 */
public class BeanDefinition {
    private Class beanClass;

    public BeanDefinition() {
    }

    public BeanDefinition(Class beanClass) {
        this.beanClass = beanClass;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }
}
