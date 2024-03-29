package com.zzzi.springframework.context.support;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.config.BeanPostProcessor;
import com.zzzi.springframework.context.ApplicationContext;
import com.zzzi.springframework.context.ApplicationContextAware;

/**@author zzzi
 * @date 2023/11/6 15:47
 * 不能直接注入的容器资源，将其包装到一个修改逻辑当中，后面触发修改逻辑自动完成注入
 */
public class ApplicationContextAwareProcessor implements BeanPostProcessor {
    //在这里定义一个成员属性，从而暂存要注入的容器资源
    private final ApplicationContext applicationContext;

    public ApplicationContextAwareProcessor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**@author zzzi
     * @date 2023/11/6 15:48
     * 触发这个修改逻辑，自动完成容器资源的注入,也就是在初始化方法执行之前注入
     * 注入的applicationContext是在refresh方法中就保存的资源
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof ApplicationContextAware){
            ((ApplicationContextAware) bean).setApplicationContext(applicationContext);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return null;
    }
}
