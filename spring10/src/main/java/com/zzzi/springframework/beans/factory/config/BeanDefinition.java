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

    /**
     * @author zzzi
     * @date 2023/11/7 9:51
     * 新增的属性，默认情况下为单例模式
     */
    private boolean singleton = true;
    private boolean prototype = false;
    String SCOPE_SINGLETON = ConfigurableBeanFactory.SCOPE_SINGLETON;
    String SCOPE_PROTOTYPE = ConfigurableBeanFactory.SCOPE_PROTOTYPE;
    private String scope = SCOPE_SINGLETON;

    public BeanDefinition(Class beanClass) {
        this(beanClass, new PropertyValues());
    }

    public BeanDefinition(Class beanClass, PropertyValues propertyValues) {
        this.beanClass = beanClass;
        this.propertyValues = propertyValues != null ? propertyValues : new PropertyValues();

    }

    /**
     * @author zzzi
     * @date 2023/11/7 9:52
     * 新增的方法
     */
    public void setScope(String scope) {
        this.scope = scope;
        this.singleton = SCOPE_SINGLETON.equals(scope);
        this.prototype = SCOPE_PROTOTYPE.equals(scope);
    }

    public boolean isSingleton() {
        return singleton;
    }

    public boolean isPrototype() {
        return prototype;
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
