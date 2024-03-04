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
        //经历这一步的beanFactory已经有了配置文件中注册的bean定义信息
        loadBeanDefinitions(beanFactory);
        this.beanFactory = beanFactory;
    }

    /**@author zzzi
     * @date 2024/3/4 14:57
     * 这个方法就是让子类的api与之前读取配置文件的api名称一样
     * 子类实现这个方法，内部调用之前读取配置文件的api完成配置文件的解析
     */
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
