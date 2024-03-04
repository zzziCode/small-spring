package com.zzzi.springframework.beans.factory.config;

/**
 * @author zzzi
 * @date 2023/10/31 19:26
 * 在这里保存bean的注册信息，不只是类信息
 * 还有属性列表
 */
public class BeanDefinition {
    //要确保两个属性都不为空
    private Class beanClass;
    //这里保存了当前bean的属性列表
    private PropertyValues propertyValues;

    public BeanDefinition() {
    }

    /**
     * @author zzzi
     * @date 2024/1/7 21:35
     * 两种类型的BeanDefinition构造函数，一种是无参的bean使用
     * 另外的一种是有参的bean使用
     */
    public BeanDefinition(Class beanClass) {
        this.beanClass = beanClass;
        this.propertyValues = new PropertyValues();
    }

    public BeanDefinition(Class beanClass, PropertyValues propertyValues) {
        this.beanClass = beanClass;
        this.propertyValues = propertyValues != null ? propertyValues : new PropertyValues();
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public PropertyValues getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(PropertyValues propertyValues) {
        this.propertyValues = propertyValues;
    }
}
