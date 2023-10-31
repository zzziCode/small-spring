package com.zzzi.springframework.beans.factory.support;


import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.BeanFactory;
import com.zzzi.springframework.beans.factory.config.BeansException;

/**
 * @author zzzi
 * @date 2023/10/31 13:30
 * 在这里实现getBean方法，并且新增两个接口供下面的类实现
 */
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory {
    @Override
    public Object getBean(String beanName) throws BeansException {
        return doGetBean(beanName, null);
    }

    @Override
    public Object getBean(String beanName, Object... args) throws BeansException {
        return doGetBean(beanName, args);
    }

    /**
     * @author zzzi
     * @date 2023/10/31 13:51
     * 将所有获取bean对象的操作集成到这一个方法中
     */
    protected Object doGetBean(final String beanName, final Object[] args) {
        //尝试获取已存在的bean
        Object bean = getSingleton(beanName);
        if (bean != null)
            return bean;

        //到这里就是没获取到bean，此时需要新建
        //1.获取bean的类信息
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        //2.根据bean的类信息以及传递来的参数创建bean对象
        return createBean(beanName, beanDefinition, args);
    }

    //下面两个方法留给下面的类实现
    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    protected abstract Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException;

}
