package com.zzzi.springframework.context.support;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.ConfigurableListableBeanFactory;
import com.zzzi.springframework.beans.factory.config.BeanFactoryPostProcessor;
import com.zzzi.springframework.beans.factory.config.BeanPostProcessor;
import com.zzzi.springframework.context.ApplicationEvent;
import com.zzzi.springframework.context.ApplicationListener;
import com.zzzi.springframework.context.ConfigurableApplicationContext;
import com.zzzi.springframework.context.event.ApplicationEventMulticaster;
import com.zzzi.springframework.context.event.ContextClosedEvent;
import com.zzzi.springframework.context.event.ContextRefreshedEvent;
import com.zzzi.springframework.context.event.SimpleApplicationEventMulticaster;
import com.zzzi.springframework.core.io.DefaultResourceLoader;

import java.util.Collection;
import java.util.Map;

/**
 * @author zzzi
 * @date 2023/11/4 15:37
 * 在这里实现refresh方法
 * 并且实现执行实例化之前的修改逻辑，保存实例化之后的修改逻辑
 * 注册钩子函数
 */
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    //在这里新增广播器的名称以及广播器
    public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Override
    public void refresh() throws BeansException {
        // 1. 创建 BeanFactory，并加载 BeanDefinition
        refreshBeanFactory();

        // 2. 获取 BeanFactory
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();
        /**@author zzzi
         * @date 2023/11/6 15:51
         * 将不能直接注入的资源暂存到一个包装处理器当中
         */
        //3. 暂存一个容器资源到包装处理器中，后期触发修改逻辑自动完成注入
        beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));

        // 4. 在 Bean 实例化之前，执行 BeanFactoryPostProcessor (Invoke factory processors registered as beans in the context.)
        invokeBeanFactoryPostProcessors(beanFactory);

        // 5. BeanPostProcessor 需要提前于其他 Bean 对象实例化之前执行注册操作
        registerBeanPostProcessors(beanFactory);

        /**@author zzzi
         * @date 2023/11/8 14:08
         * 新增了三步
         */
        //6. 初始化一个事件发布者：事件广播器
        initApplicationEventMulticaster();

        //7. 保存所有的事件监听器到广播器的容器中
        registerListeners();

        // 8. 提前实例化单例Bean对象
        //只有单例bean才会被保存
        beanFactory.preInstantiateSingletons();

        //9. 容器刷新完成，发布容器刷新完成事件
        finishRefresh();
    }

    //发布容器刷新完毕事件
    private void finishRefresh() {
        publishEvent(new ContextRefreshedEvent(this));
    }

    //拿到所有的监听器，一一保存到广播器的容器中
    private void registerListeners() {
        Collection<ApplicationListener> applicationListeners = getBeansOfType(ApplicationListener.class).values();
        for (ApplicationListener listener : applicationListeners) {
            applicationEventMulticaster.addApplicationListener(listener);
        }
    }

    //创建一个简单的广播器对象，并将其保存到容器中
    private void initApplicationEventMulticaster() {
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();
        applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
        beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, applicationEventMulticaster);
    }

    //保存实例化后的修改操作
    private void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        Map<String, BeanPostProcessor> postProcessorMap = beanFactory.getBeansOfType(BeanPostProcessor.class);
        for (BeanPostProcessor postProcessor : postProcessorMap.values()) {
            beanFactory.addBeanPostProcessor(postProcessor);
        }
    }

    //执行实例化前的修改操作
    private void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        Map<String, BeanFactoryPostProcessor> factoryPostProcessorMap = beanFactory.getBeansOfType(BeanFactoryPostProcessor.class);
        for (BeanFactoryPostProcessor factoryPostProcessor : factoryPostProcessorMap.values()) {
            factoryPostProcessor.postProcessBeanFactory(beanFactory);
        }
    }

    @Override
    public void publishEvent(ApplicationEvent event) {
        applicationEventMulticaster.multicastEvent(event);
    }

    /**
     * @author zzzi
     * @date 2023/11/3 18:40
     * 这个钩子函数注册了一个销毁的时机，在虚拟机退出之前执行销毁的逻辑
     */
    @Override
    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
    }

    @Override
    public void close() {
        //1. 发布容器关闭事件
        publishEvent(new ContextClosedEvent(this));

        //2. 执行销毁方法
        getBeanFactory().destroySingletons();
    }

    protected abstract void refreshBeanFactory() throws BeansException;

    protected abstract ConfigurableListableBeanFactory getBeanFactory();

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        return getBeanFactory().getBeansOfType(type);
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return getBeanFactory().getBeanDefinitionNames();
    }

    @Override
    public Object getBean(String name) throws BeansException {
        return getBeanFactory().getBean(name);
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        return getBeanFactory().getBean(name, args);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return getBeanFactory().getBean(name, requiredType);
    }

}
