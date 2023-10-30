package com.zzzi.springframework.beans.factory.support;


import com.zzzi.springframework.beans.factory.config.BeanDefinitionRegistry;
import com.zzzi.springframework.beans.factory.config.BeansException;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zzzi
 * @date 2023/10/30 12:59
 * 在这里将之前定义的所有类中的方法都继承过来,里面有两个抽象方法需要实现
 * 其余的方法都在上面有了实现,每个类各司其职
 * 这个类中继承而来的方法有:
 * 1.registerBeanDefinition:绑定bean的类信息与其名称之间的映射关系
 * 2.getBeanDefinition:根据bean的名称获取类信息从而利用反射创建bean的实例化对象
 * 3.createBean:根据得到的类信息反射得到一个bean的对象
 * 4.getBean:尝试获取一个bean对象,没有就创建并保存
 * 5.getSingleton:尝试获取一个单例模式的bean对象
 * 6.addSingleton:将新创建的bean对象保存到容器中
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements BeanDefinitionRegistry {
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    /**
     * @author zzzi
     * @date 2023/10/30 13:00
     * 根据传递过来的bean的类信息以及其名称
     * 将这个映射保存到对应的容器中
     */
    @Override
    public void registerBeanDefinition(String name, BeanDefinition beanDefinition) {
    beanDefinitionMap.put(name,beanDefinition);
    }

    /**@author zzzi
     * @date 2023/10/30 13:01
     * 根据传递来的bean的名称获取到其类信息
     * 调用这个方法是为了获取到类信息从而利用反射创建这个bean的实例化对象
     */
    @Override
    protected BeanDefinition getBeanDefinition(String beanName) throws BeansException {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if(beanDefinition==null)
            throw new BeansException("No bean named '"+beanName+"' is defined");
        return beanDefinition;
    }
}
