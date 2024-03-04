package com.zzzi.springframework.beans.factory.support;


import com.zzzi.springframework.beans.factory.config.BeansException;
import com.zzzi.springframework.beans.factory.config.BeanFactory;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;

/**@author zzzi
 * @date 2023/10/30 12:48
 * 继承了一个类,实现了一个接口
 * DefaultSingletonBeanRegistry类中的方法已经全部实现了,在这里继承的目的只是为了使用父类的方法
 * BeanFactory接口中的方法还没有实现,在这里的目的就是为了实现其中的方法
 *
 * 同时提供了两个抽象方法供下面的类实现
 *
 * 这个类继承的到的方法有:
 * 1.getBean:尝试获取一个单例模式的bean对象,没有就创建
 * 2.getSingleton:尝试获取一个单例模式的bean对象并返回
 * 3.addSingleton:将新创建的bean对象保存到容器中
 */
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory {
    /**@author zzzi
     * @date 2023/10/30 12:49
     * 将这个唯一的没有实现的方法在这里实现
     * 尝试获取一个单例模式的bean对象
     * 有就直接返回,没有就先创建再返回
     */
    @Override
    public Object getBean(String name) throws BeansException {
        //尝试直接获取，这里保证了bean的单例
        Object singleton = getSingleton(name);
        if(singleton!=null){
            return singleton;
        }

        //到了这里就是当前bean还没有实例化,所以需要实例化一个单例模式的bean对象

        //1.获取当前bean的类信息,便于使用反射来创建对象
        BeanDefinition beanDefinition = getBeanDefinition(name);

        //2.根据类信息创建bean的实例化对象,并将其保存到容器中
        return createBean(name,beanDefinition);
    }

    /**@author zzzi
     * @date 2023/10/29 21:43
     * 在类DefaultListableBeanFactory中实现
     */
    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    /**@author zzzi
     * @date 2023/10/29 21:44
     * 在类AbstractAutowireCapableBeanFactory中实现
     */
    protected abstract Object createBean(String beanName, BeanDefinition beanDefinition) throws BeansException;

}
