package com.zzzi.springframework.common;


import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.PropertyValue;
import com.zzzi.springframework.beans.PropertyValues;
import com.zzzi.springframework.beans.factory.ConfigurableListableBeanFactory;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.BeanFactoryPostProcessor;

public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        BeanDefinition beanDefinition = beanFactory.getBeanDefinition("userService");
        PropertyValues propertyValues = beanDefinition.getPropertyValues();
        //增加一个字段
        propertyValues.addPropertyValue(new PropertyValue("company", "改为：字节跳动"));
    }
}
