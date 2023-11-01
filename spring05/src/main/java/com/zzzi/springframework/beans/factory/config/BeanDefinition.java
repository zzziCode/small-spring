package com.zzzi.springframework.beans.factory.config;

import com.zzzi.springframework.beans.PropertyValues;

/**
 * @author zzzi
 * @date 2023/11/1 13:44
 * 在这里保存bean的注册信息
 * 包括bean的类信息和其依赖的属性列表
 */
public class BeanDefinition {
    //要确保两个属性都不为空
    private Class beanClass;
    private PropertyValues propertyValues;

    //对外提供只注册类信息的接口，因为部分bean没有属性
    public BeanDefinition(Class beanClass) {
        this.beanClass = beanClass;
        this.propertyValues = new PropertyValues();
    }

    public BeanDefinition() {
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
