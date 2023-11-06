package com.zzzi.springframework.beans.factory.config;

import com.zzzi.springframework.beans.PropertyValues;

/**
 * @author zzzi
 * @date 2023/11/4 13:15
 * 在这里保存bean的注册信息
 */
public class BeanDefinition {
    private Class beanClass;
    private PropertyValues propertyValues;
    //这两个属性不是必须的，所以不用在构造函数中给出
    private String initMethodName;
    private String destroyMethodName;

    public BeanDefinition(Class beanClass) {
        this(beanClass, new PropertyValues());
    }

    public BeanDefinition(Class beanClass, PropertyValues propertyValues) {
        this.beanClass = beanClass;
        this.propertyValues = propertyValues != null ? propertyValues : new PropertyValues();

    }

    //getter和setter方法
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

    public String getInitMethodName() {
        return initMethodName;
    }

    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    public String getDestroyMethodName() {
        return destroyMethodName;
    }

    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }
}
