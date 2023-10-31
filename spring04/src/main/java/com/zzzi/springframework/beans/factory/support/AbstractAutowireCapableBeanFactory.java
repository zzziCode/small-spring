package com.zzzi.springframework.beans.factory.support;

import cn.hutool.core.bean.BeanUtil;
import com.zzzi.springframework.beans.factory.config.*;

import java.lang.reflect.Constructor;

/**
 * @author zzzi
 * @date 2023/10/31 19:44
 * 在这里实现createBean的方法，将bean的创建和属性填充分开
 * 并且内部使用了实例化的策略来创建带参的bean对象
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory {
    //保存当前的实例化策略
    private InstantiationStrategy instantiationStrategy = new CglibSubclassingInstantistionStrategy();


    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
        Object bean = null;
        try {
            /**@author zzzi
             * @date 2023/10/31 19:47
             * 在这里将创建bean和属性填充分开
             */
            //创建空bean对象
            bean = createBeanInstance(beanName, beanDefinition, args);
            //属性填充
            applyPropertyValues(beanName, bean, beanDefinition);
        } catch (Exception e) {
            throw new BeansException("Instantiation of bean failed", e);
        }
        //将创建得到的bean对象保存到IOC容器中并返回
        addSingleton(beanName, bean);
        return bean;
    }

    /**
     * @author zzzi
     * @date 2023/10/31 19:54
     * 获取一个bean对象
     */
    private Object createBeanInstance(String beanName, BeanDefinition beanDefinition, Object[] args) {
        //如果参数列表为空，直接调用实例化策略创建无参对象并返回
        if (args == null)
            return getInstantiationStrategy().instantiate(beanDefinition, beanName, null, null);

        Constructor constructorToUse = null;
        //得到当前bean的类信息，从而得到当前bean的所有构造函数
        Class beanClass = beanDefinition.getBeanClass();
        Constructor[] constructors = beanClass.getDeclaredConstructors();
        //遍历所有的构造函数，尝试找到目标构造函数
        for (Constructor constructor : constructors) {
            Class[] parameterTypes = constructor.getParameterTypes();
            //参数列表的长度都不匹配，当前构造函数肯定不是目标构造函数
            if (parameterTypes.length != args.length)
                continue;
            int i = 0;
            for (; i < parameterTypes.length; i++) {
                //匹配的过程中遇到参数类型不相等
                if (parameterTypes[i] != args[i].getClass())
                    break;
            }
            //如果匹配到了最后，说明参数列表匹配成功，找到了目标构造函数
            if (i == parameterTypes.length) {
                constructorToUse = constructor;
                break;
            }
        }
        //利用构造函数和当前的实例化策略创建一个bean对象并返回
        return this.getInstantiationStrategy().instantiate(beanDefinition, beanName, constructorToUse, args);
    }

    /**
     * @author zzzi
     * @date 2023/10/31 19:55
     * 做属性填充,传递过来的beanName没有用处
     */
    private void applyPropertyValues(String beanName, Object bean, BeanDefinition beanDefinition) {
        try {
            PropertyValues definitionPropertyValues = beanDefinition.getPropertyValues();
            //如果当前bean没有属性列表，就不用填充
            if(definitionPropertyValues==null)
                return;
            PropertyValue[] propertyValues = definitionPropertyValues.getPropertyValues();
            //遍历属性列表，进行属性填充
            for (PropertyValue propertyValue : propertyValues) {
                String name = propertyValue.getName();
                Object value = propertyValue.getValue();
                /**@author zzzi
                 * @date 2023/10/31 19:58
                 * 属性填充时，当前属性是一个bean类型，需要单独处理
                 * 在这时才创建当前属性所依赖的bean
                 */
                if (value instanceof BeanReference) {
                    BeanReference beanReference = (BeanReference) value;
                    //获取这个所依赖的bean的姓名，并且按照这个姓名尝试获取一个bean对象
                    value = getBean(beanReference.getBeanName());
                }

                //属性填充
                BeanUtil.setFieldValue(bean, name, value);
            }
        } catch (Exception e) {
            throw new BeansException("Error setting property values：" + beanName);
        }
    }

    public InstantiationStrategy getInstantiationStrategy() {
        return instantiationStrategy;
    }
}
