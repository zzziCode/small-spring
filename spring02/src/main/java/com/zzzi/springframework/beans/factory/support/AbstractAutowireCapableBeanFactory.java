package com.zzzi.springframework.beans.factory.support;


import com.zzzi.springframework.beans.factory.config.BeansException;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;

/**@author zzzi
 * @date 2023/10/30 12:42
 * 在这里只是实现了父类AbstractBeanFactory中提供的一个方法
 * 每个类各司其职
 * 这个类继承而来的方法有:
 * 1.getBean
 * 2.getSingleton
 * 3.addSingleton
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory {

    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition) throws BeansException {
        Object bean;
        try {
            //利用反射,根据传递来的类信息创建一个实例化对象，此时相当于将bean的实例化交给了ioc容器
            bean = beanDefinition.getBeanClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BeansException("Instantiation of bean failed", e);
        }

        //在这里就是创建成功
        //调用继承自DefaultSingletonBeanRegistry中的addSingleton方法将这个对象保存到容器中
        addSingleton(beanName, bean);
        //将创建的bean返回，返回值用于getBean的返回值
        return bean;
    }
}
