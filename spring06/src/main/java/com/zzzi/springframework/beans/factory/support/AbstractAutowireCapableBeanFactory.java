package com.zzzi.springframework.beans.factory.support;

import cn.hutool.core.bean.BeanUtil;
import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.PropertyValue;
import com.zzzi.springframework.beans.PropertyValues;
import com.zzzi.springframework.beans.factory.config.AutowireCapableBeanFactory;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.BeanPostProcessor;
import com.zzzi.springframework.beans.factory.config.BeanReference;

import java.lang.reflect.Constructor;

/**
 * @author zzzi
 * @date 2023/11/1 14:09
 * 在这里实现创建bean的方法
 * 分为三步：
 * 1. 空bean的创建
 * 2. bean的属性填充
 * 3. 实例化后修改策略的执行
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {
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
            //3. 执行修改策略
            bean=initializeBean(beanName,bean,beanDefinition);
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
    /**@author zzzi
     * @date 2023/11/3 12:32
     * 在这里增加执行修改策略的方法
     */
    private Object initializeBean(String beanName, Object bean, BeanDefinition beanDefinition) {
        Object wrappedBean=applyBeanPostProcessorsBeforeInitialization(bean,beanName);

        invokeInitMethods(beanName,wrappedBean,beanDefinition);

        wrappedBean=applyBeanPostProcessorsAfterInitialization(wrappedBean,beanName);

        return wrappedBean;
    }
    /**@author zzzi
     * @date 2024/3/4 14:35
     * 这个方法后期会执行一些初始化的工作
     */
    private void invokeInitMethods(String beanName, Object wrappedBean, BeanDefinition beanDefinition) {
    }

    /**@author zzzi
     * @date 2023/11/3 12:39
     * 执行实例化后的修改逻辑，之后会执行一个初始化的工作，初始化之后还会调用一个初始化后的逻辑
     */
    @Override
    public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) throws BeansException {
        //得到要进行修改的实例化bean对象
        Object result = existingBean;
        //遍历所有的修改措施，每一个措施都进行修改
        for (BeanPostProcessor processor : getBeanPostProcessors()) {
            //调用实现了bean实例化之后修改接口的类中的修改策略，修改已经实例化的bean
            Object current = processor.postProcessBeforeInitialization(result, beanName);
            if (null == current) return result;
            result = current;
        }
        return result;
    }
    /**@author zzzi
     * @date 2023/11/3 12:43
     * 执行实例化后的修改逻辑
     */
    @Override
    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) throws BeansException {
        //得到要进行修改的实例化bean对象
        Object result = existingBean;
        //遍历所有的修改措施，每一个措施都进行修改
        for (BeanPostProcessor processor : getBeanPostProcessors()) {
            //调用实现了bean实例化之后修改接口的类中的修改策略，修改已经实例化的bean
            Object current = processor.postProcessAfterInitialization(result, beanName);
            if (null == current) return result;
            result = current;
        }
        return result;
    }

    public InstantiationStrategy getInstantiationStrategy() {
        return instantiationStrategy;
    }

    public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
        this.instantiationStrategy = instantiationStrategy;
    }
}
