package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.ConfigurableListableBeanFactory;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zzzi
 * @date 2023/11/4 15:09
 * 这里是spring中的核心类，之前的项目以这里为入口
 * 现在的项目将这个入口进行了封装，由于继承和实现了很多的类和接口，所以这个类中有很多的方法
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements BeanDefinitionRegistry, ConfigurableListableBeanFactory {
    //这里保存所有的bean的注册信息，用这些注册信息来实例化bean对象
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    @Override
    public void preInstantiateSingletons() throws BeansException {
        Set<String> beanNames = beanDefinitionMap.keySet();
        for (String beanName : beanNames) {
            //单例对象才全部实例化，原型模式的bean不用实例化，这与spring的设计思想一致
            if (beanDefinitionMap.get(beanName).isSingleton())
                //只要获取这个bean对象，不存在的话就会自动新建
                getBean(beanName);
        }
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        Map<String, T> result = new HashMap<>();
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            Class beanClass = beanDefinition.getBeanClass();
            if (type.isAssignableFrom(beanClass)) {
                result.put(beanName, (T) getBean(beanName));
            }
        });
        return result;
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanName, beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws BeansException {
        return beanDefinitionMap.get(beanName);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[0]);
    }

    /**
     * @author zzzi
     * @date 2023/11/13 16:14
     * 在这里新增一个单按照类型获取bean对象的方法
     */
    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        List<String> beanNames = new ArrayList<>();
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            Class beanClass = entry.getValue().getBeanClass();
            if (requiredType.isAssignableFrom(beanClass)) {
                beanNames.add(entry.getKey()); 
            }
        }
        //如果一个类型下有一个对象才返回
        if (1 == beanNames.size()) {
            return getBean(beanNames.get(0), requiredType);
        }
        //一个类型下有多个bean对象就抛出异常，这种异常就是需要一个bean，但是找到了多个，不知道返回哪一个
        throw new BeansException(requiredType + "expected single bean but found " + beanNames.size() + ": " + beanNames);
    }

}
