package com.zzzi.springframework.beans.factory.support;


import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.BeansException;
import com.zzzi.springframework.beans.factory.config.InstantiationStrategy;

import java.lang.reflect.Constructor;

/**
 * @author zzzi
 * @date 2023/10/31 14:08
 * 这里只关注如何创建带参的bean对象，主要是引入了一个实例化策略模块，利用他们提供的接口创建对应的bean对选哪个
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory {
    //保存一个成员变量，来指定当前使用什么实例化策略
    private InstantiationStrategy instantiationStrategy = new CglibSubclassingInstantiationStrategy();

    /**
     * @author zzzi
     * @date 2023/10/31 14:29
     * 在这里对原始的createBean方法进行了增强，引入了实例化策略
     * 并且根据构造函数的参数列表找到对应的有参构造创建对象
     */
    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
        Object bean = null;
        try {
            bean = createBeanInstance(beanDefinition, beanName, args);
        } catch (Exception e) {
            throw new BeansException("Instantiation of bean failed", (ReflectiveOperationException) e);
        }
        //将创建好的bean对象保存到IOC容器中并返回
        addSingleton(beanName, bean);
        return bean;
    }

    /**
     * @author zzzi
     * @date 2023/10/31 14:10
     * 这里是本项目的核心，利用实例化策略来创建带参的bean对象
     * 主要是根据参数列表找到匹配的构造函数，然后将构造函数和其他信息一同instantiate方法
     * 在这个实例化策略方法内部完成对带参bean对象的创建
     */
    private Object createBeanInstance(BeanDefinition beanDefinition, String beanName, Object[] args) {
        //如果参数列表为空，直接调用实例化策略创建无参对象并返回
        if (args == null)
            return getInstantiationStrategy().instantiate(beanDefinition, beanName, null, null);

        //在这里说明需要创建带参bean对象
        //1.尝试找到目标构造函数
        Constructor constructorToUse = null;

        //获取bean对象的类信息
        Class beanClass = beanDefinition.getBeanClass();
        //获取到这个bean对象的所有构造函数
        Constructor[] declaredConstructors = beanClass.getDeclaredConstructors();
        //遍历所有的构造函数，用参数列表去匹配，从而找到目标构造函数
        for (Constructor declaredConstructor : declaredConstructors) {
            //获取到当前构造函数的参数列表
            Class[] parameterTypes = declaredConstructor.getParameterTypes();
            //参数个数都不一样，肯定不匹配,继续查找下一个
            if (parameterTypes.length != args.length)
                continue;
            //参数个数一样，看类型是否一样
            int i = 0;
            for (; i < parameterTypes.length; i++) {
                //参数列表的类型不匹配,继续搜索合适的构造函数
                if (parameterTypes[i] != args[i].getClass())
                    break;
            }
            //参数列表匹配到了末尾说明找到目标构造函数
            if (i == parameterTypes.length) {
                constructorToUse = declaredConstructor;
                break;
            }
        }
        //在这里创建带参的bean对象
        return getInstantiationStrategy().instantiate(beanDefinition, beanName, constructorToUse, args);
    }

    public InstantiationStrategy getInstantiationStrategy() {
        return instantiationStrategy;
    }
}
