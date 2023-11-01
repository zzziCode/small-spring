package com.zzzi.springframework.beans.factory.config;

/**
 * @author zzzi
 * @date 2023/11/1 13:43
 * 如果bean的属性依赖另外一个bean
 * 那么就在这里保存另外一个bean的名称，相当于建立依赖关系
 */
public class BeanReference {
    private final String beanName;

    public BeanReference(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }
}
