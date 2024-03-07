package com.zzzi.springframework.context.event;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.BeanFactory;
import com.zzzi.springframework.beans.factory.BeanFactoryAware;
import com.zzzi.springframework.context.ApplicationEvent;
import com.zzzi.springframework.context.ApplicationListener;
import com.zzzi.springframework.util.ClassUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author zzzi
 * @date 2023/11/8 13:24
 * 抽象广播器类，在其中完成初始化的操作
 */
public abstract class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster, BeanFactoryAware {
    //通过Aware感知获取到beanFactory
    private BeanFactory beanFactory;
    //保存所有的事件监听器
    private final Set<ApplicationListener<ApplicationEvent>> applicationListeners = new HashSet<>();

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        applicationListeners.add((ApplicationListener<ApplicationEvent>) listener);
    }

    @Override
    public void removeApplicationListener(ApplicationListener<?> listener) {
        applicationListeners.remove(listener);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory=beanFactory;
    }
    /**@author zzzi
     * @date 2023/11/8 13:33
     * 在下面添加两个最核心的方法，根据传递来的事件拿到所有匹配的监听器
     */
    protected Set<ApplicationListener<ApplicationEvent>> getApplicationListeners(ApplicationEvent event){
        Set<ApplicationListener<ApplicationEvent>> matchedListeners=new HashSet<>();
        //根据当前事件找到所有匹配的事件监听器
        for (ApplicationListener<ApplicationEvent> listener : applicationListeners) {
            //查看监听器监听的事件类型与当前事件类型的类之间的关系，匹配就为true
            if(supportEvent(listener,event)){
                matchedListeners.add(listener);
            }
        }
        return matchedListeners;
    }
    /**@author zzzi
     * @date 2023/11/8 13:41
     * 这个方法判断当前监听器是不是监听的当前事件
     */
    private boolean supportEvent(ApplicationListener<ApplicationEvent> listener, ApplicationEvent event) {
        Class<? extends ApplicationListener> listenerClass = listener.getClass();

        // 按照 CglibSubclassingInstantiationStrategy、SimpleInstantiationStrategy 不同的实例化类型，需要判断后获取目标 class
        //Cglib实例化后真正的class类型是父类，普通反射实例化后的真正class类型就是其本身，根据这个拿到目标的class
        Class<?> targetClass = ClassUtils.isCglibProxyClass(listenerClass) ? listenerClass.getSuperclass() : listenerClass;
        //拿到事件监听器的第一个泛型
        Type genericInterface = targetClass.getGenericInterfaces()[0];

        //拿到泛型的实际类型
        Type actualTypeArgument = ((ParameterizedType) genericInterface).getActualTypeArguments()[0];
        //拿到监听器泛型中执行的类名
        String className = actualTypeArgument.getTypeName();
        Class<?> eventClassName;
        try {
            //拿到这个类名对应的全限定名
            eventClassName = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new BeansException("wrong event class name: " + className);
        }
        // 如果A.isAssignableFrom(B)结果是true，证明B可以转换成为A,也就是A可以由B转换而来。
        //判断监听器监听的全限定名和这个事件的全限定名之间是否可以相互转换
        return eventClassName.isAssignableFrom(event.getClass());
    }
}
