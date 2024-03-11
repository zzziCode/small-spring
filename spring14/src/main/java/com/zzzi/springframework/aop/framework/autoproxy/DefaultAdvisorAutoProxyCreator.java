package com.zzzi.springframework.aop.framework.autoproxy;

import com.zzzi.springframework.aop.*;
import com.zzzi.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import com.zzzi.springframework.aop.framework.ProxyFactory;
import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.PropertyValues;
import com.zzzi.springframework.beans.factory.BeanFactory;
import com.zzzi.springframework.beans.factory.BeanFactoryAware;
import com.zzzi.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import com.zzzi.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;

import java.util.Collection;

/**
 * @author zzzi
 * @date 2023/11/11 19:35
 * 整个项目的核心类
 */
public class DefaultAdvisorAutoProxyCreator implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {
    private DefaultListableBeanFactory beanFactory;

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        return true;
    }

    /**
     * @author zzzi
     * @date 2023/11/13 15:42
     * 新增的实现方法，不在这里进行注解属性填充，所以返回原值pvs
     */
    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        return null;
    }

    private boolean isInfrastructureClass(Class<?> beanClass) {
        return Advice.class.isAssignableFrom(beanClass) || Pointcut.class.isAssignableFrom(beanClass) || Advisor.class.isAssignableFrom(beanClass);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * @author zzzi
     * @date 2023/11/14 13:47
     * 将代理对象的创建时机转移到这里
     * 此时需要被代理的bean属性填充完成，此时被代理之后属性也有值了
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        if (isInfrastructureClass(beanClass))
            return bean;

        //拿到配置的代理信息
        Collection<AspectJExpressionPointcutAdvisor> advisors = beanFactory.getBeansOfType(AspectJExpressionPointcutAdvisor.class).values();
        for (AspectJExpressionPointcutAdvisor advisor : advisors) {
            //拿到类匹配器，从而判断当前类是否需要代理
            ClassFilter classFilter = advisor.getPointcut().getClassFilter();
            //当前切入点表达式不匹配当前bean，尝试下一个
            if (!classFilter.matches(beanClass))
                continue;
            //在这里就是匹配成功，准备创建代理对象

            AdvisedSupport advisedSupport = new AdvisedSupport();
            /**@author zzzi
             * @date 2023/11/14 13:47
             * 这里保存的是已经属性填充过的bean，而不是去新建一个空的bean
             */
            TargetSource targetSource = new TargetSource(bean);

            //填充创建代理对象所需要的参数

            advisedSupport.setTargetSource(targetSource);
            advisedSupport.setMethodInterceptor((MethodInterceptor) advisor.getAdvice());
            advisedSupport.setMethodMatcher(advisor.getPointcut().getMethodMatcher());
            advisedSupport.setProxyTargetClass(true);

            //调用代理工厂中的方法得到一个代理对象并返回
            return new ProxyFactory(advisedSupport).getProxy();
        }
        //当前bean没有创建成功代理对象，就返回空
        return bean;
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }
}
