package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.ConfigurableListableBeanFactory;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zzzi
 * @date 2023/11/1 14:28
 * 在这里实现保存bean注册信息，获取bean注册信息等方法
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements BeanDefinitionRegistry , ConfigurableListableBeanFactory {
    //使用一个容器保存所有bean的注册信息
    Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanName,beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if(beanDefinition==null)
            throw new BeansException("No bean named '" + beanName + "' is defined");
        return beanDefinition;
    }

    /**@author zzzi
     * @date 2024/3/4 15:07
     * 这个方法完成了所有空bean的实例化，正常this::getBean会得到一个返回的bean，只是这里没有接收
     * 单纯的利用getBean可以实例化bean的功能将所有的bean进行实例化
     */
    @Override
    public void preInstantiateSingletons() throws BeansException {
        beanDefinitionMap.keySet().forEach(this::getBean);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return beanDefinitionMap.containsKey(beanName);
    }

    /**@author zzzi
     * @date 2023/11/2 15:26
     * 按照类型获取bean对象，将存储这些符合要求的bean对象的容器返回
     */
    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        Map<String, T> result = new HashMap<>();
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            //获取每一个bean的定义
            Class beanClass = beanDefinition.getBeanClass();
            if (type.isAssignableFrom(beanClass)) {
                result.put(beanName, (T) getBean(beanName));
            }
        });
        return result;
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[0]);
    }

}
