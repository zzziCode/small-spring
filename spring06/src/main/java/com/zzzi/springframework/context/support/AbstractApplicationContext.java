package com.zzzi.springframework.context.support;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.ConfigurableListableBeanFactory;
import com.zzzi.springframework.beans.factory.config.BeanFactoryPostProcessor;
import com.zzzi.springframework.beans.factory.config.BeanPostProcessor;
import com.zzzi.springframework.context.ConfigurableApplicationContext;
import com.zzzi.springframework.core.io.DefaultResourceLoader;

import java.util.Map;
/**@author zzzi
 * @date 2023/11/3 14:55
 * 在这里实现refresh方法，并继承DefaultResourceLoader这个类
 * 为了获取资源加载器
 */
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    @Override
    public void refresh() throws BeansException {
        //1.创建beanFactory
        refreshBeanFactory();

        //2.获取beanFactory
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();

        /**@author zzzi
         * @date 2023/11/3 13:20
         * 上面两步相当与得到了beanFactory对象，并且从配置文件中得到了注册信息
         */
        //3.实例化之前执行修改操作
        invokeBeanFactoryPostProcessors(beanFactory);

        //4.保存实例化之后的修改操作
        registerBeanPostProcessors(beanFactory);

        //实例化所有bean对象
        beanFactory.preInstantiateSingletons();
    }

    /**@author zzzi
     * @date 2023/11/3 13:00
     * 保存实例化后的修改操作
     */
    protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory){
        Map<String, BeanPostProcessor> postProcessorMap = beanFactory.getBeansOfType(BeanPostProcessor.class);
        for (BeanPostProcessor postProcessor : postProcessorMap.values()) {
            beanFactory.addBeanPostProcessor(postProcessor);
        }
    }

    /**@author zzzi
     * @date 2023/11/3 13:00
     * 执行实例化前的修改操作
     */
    private void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        Map<String, BeanFactoryPostProcessor> factoryPostProcessorMap = beanFactory.getBeansOfType(BeanFactoryPostProcessor.class);
        for (BeanFactoryPostProcessor factoryPostProcessor : factoryPostProcessorMap.values()) {
            factoryPostProcessor.postProcessBeanFactory(beanFactory);
        }
    }

    //为了创建一个beanFactory
    protected abstract void refreshBeanFactory() throws BeansException;

    //获取得到一个beanFactory
    protected abstract ConfigurableListableBeanFactory getBeanFactory();

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        return getBeanFactory().getBeansOfType(type);
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return getBeanFactory().getBeanDefinitionNames();
    }

    /**@author zzzi
     * @date 2023/11/3 15:10
     * 调用这些getBean方法会最终调用到AbstractBeanFactory中的getBean
     */
    @Override
    public Object getBean(String beanName) throws BeansException {
        return getBeanFactory().getBean(beanName);
    }

    @Override
    public Object getBean(String beanName, Object... args) throws BeansException {
        return getBeanFactory().getBean(beanName,args);
    }

    @Override
    public <T> T getBean(String beanName, Class<T> requireType) throws BeansException {
        return getBeanFactory().getBean(beanName,requireType);
    }
}
