package com.zzzi.springframework.context.support;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.ConfigurableListableBeanFactory;
import com.zzzi.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * @author zzzi
 * @date 2023/11/3 13:03
 * 在这里实现beanFactory的创建
 */
public abstract class AbstractRefreshableApplicationContext extends AbstractApplicationContext {
    //主要是初始化这个变量
    private DefaultListableBeanFactory beanFactory;

    @Override
    protected void refreshBeanFactory() throws BeansException {
        DefaultListableBeanFactory beanFactory = createBeanFactory();
        loadBeanDefinitions(beanFactory);
        this.beanFactory = beanFactory;
    }

    protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory);

    private DefaultListableBeanFactory createBeanFactory() {
        //创建这个类的对象，代表可以获取到一个beanFactory，但是还没有从配置文件中读取信息
        return new DefaultListableBeanFactory();
    }

    @Override
    protected ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }
}
