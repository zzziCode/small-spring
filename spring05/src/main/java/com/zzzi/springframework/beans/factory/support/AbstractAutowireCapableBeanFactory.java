package com.zzzi.springframework.beans.factory.support;

import cn.hutool.core.bean.BeanUtil;
import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.PropertyValue;
import com.zzzi.springframework.beans.PropertyValues;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.BeanReference;

import java.lang.reflect.Constructor;

/**
 * @author zzzi
 * @date 2023/11/1 14:09
 * 在这里实现创建bean的方法
 * 分为两步：
 * 1. 空bean的创建
 * 2. bean的属性填充
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory {
    //在这里指定实例化策略
    private InstantiationStrategy instantiationStrategy = new CglibSubclassingInstantiationStrategy();


    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
        Object bean = null;
        try {
            //1. 空bean的创建
            bean = createBeanInstance(beanName, beanDefinition, args);
            //2. bean的属性填充
            applyPropertyValues(beanName, bean, beanDefinition);
        } catch (Exception e) {
            throw new BeansException("Instantiation of bean failed", e);
        }
        //创建完新的bean对象你给之后，将其保存到容器中
        addSingleton(beanName, bean);
        return bean;
    }

    //空bean的创建，需要用到实例化策略
    private Object createBeanInstance(String beanName, BeanDefinition beanDefinition, Object[] args) {
        //参数列表为空，直接调用无参构造
        if (args==null||args.length == 0)
            return getInstantiationStrategy().instantiate(beanDefinition, beanName, null, null);
        //尝试找到匹配的构造函数
        Constructor constructorToUse = null;
        Class beanClass = beanDefinition.getBeanClass();
        Constructor[] constructors = beanClass.getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            //获取当前构造函数的参数列表
            Class[] parameterTypes = constructor.getParameterTypes();
            //参数长度都不一样，肯定不匹配
            if (parameterTypes.length != args.length)
                continue;
            int i = 0;
            for (; i < parameterTypes.length; i++) {
                if (parameterTypes[i] != args[i].getClass())
                    break;
            }
            //匹配到了末尾，说明参数列表匹配成功，找到了目标构造函数
            if (i == parameterTypes.length) {
                constructorToUse = constructor;
                break;
            }
        }
        return getInstantiationStrategy().instantiate(beanDefinition, beanName, constructorToUse, args);
    }

    private void applyPropertyValues(String beanName, Object bean, BeanDefinition beanDefinition) {
        //获取属性列表
        PropertyValues values = beanDefinition.getPropertyValues();
        //没有属性列表，就不用属性填充
        if (values == null)
            return;
        //遍历属性列表，依次填充
        PropertyValue[] propertyValues = values.getPropertyValues();
        for (PropertyValue propertyValue : propertyValues) {
            String name = propertyValue.getName();
            Object value = propertyValue.getValue();

            //当前属性是另外一个bean，需要单独处理
            if (value instanceof BeanReference) {
                BeanReference beanReference = (BeanReference) value;
                //按照名称获取到这个bean，然后让value成为真正的bean
                value = getBean(beanReference.getBeanName());
            }
            //属性填充
            BeanUtil.setFieldValue(bean, name, value);
        }
    }

    public InstantiationStrategy getInstantiationStrategy() {
        return instantiationStrategy;
    }

    public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
        this.instantiationStrategy = instantiationStrategy;
    }
}
