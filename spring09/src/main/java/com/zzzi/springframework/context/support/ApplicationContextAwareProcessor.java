package com.zzzi.springframework.context.support;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.config.BeanPostProcessor;
import com.zzzi.springframework.context.ApplicationContext;
import com.zzzi.springframework.context.ApplicationContextAware;

/**@author zzzi
 * @date 2023/11/6 15:47
 * 不能直接注入的容器资源，将其包装到一个修改逻辑当中，后面触发修改逻辑自动完成注入
 * 一旦new出来这个bean的后置处理器，那么就会判断每个bean是否需要applicationContext
 * 需要就直接进行注入，这个判断逻辑在初始化之前
 */
public class ApplicationContextAwareProcessor implements BeanPostProcessor {
    //在这里定义一个成员属性，从而暂存要注入的容器资源
    private final ApplicationContext applicationContext;

    public ApplicationContextAwareProcessor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**@author zzzi
     * @date 2023/11/6 15:48
     * 触发这个修改逻辑，自动完成容器资源的注入
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
