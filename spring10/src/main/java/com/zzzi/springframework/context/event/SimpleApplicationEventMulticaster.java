package com.zzzi.springframework.context.event;

import com.zzzi.springframework.beans.factory.BeanFactory;
import com.zzzi.springframework.context.ApplicationEvent;
import com.zzzi.springframework.context.ApplicationListener;

import java.util.Set;
/**@author zzzi
 * @date 2023/11/8 13:53
 * 最后要使用的广播器
 */
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster{
    //利用set方法完成对beanFactory的容器资源注入
    public SimpleApplicationEventMulticaster(BeanFactory beanFactory) {
        setBeanFactory(beanFactory);
    }
    /**@author zzzi
     * @date 2023/11/8 13:50
     * 事件机制的核心方法，根据传递来的事件，找到匹配的事件监听器
     * 从而触发事件监听器的执行
     */
    @Override
    public void multicastEvent(final ApplicationEvent event) {
        //1. 获取到所有匹配的监听器
        Set<ApplicationListener<ApplicationEvent>> listeners = getApplicationListeners(event);
        //2. 依次触发监听器的执行
        for (ApplicationListener<ApplicationEvent> listener : listeners) {
            /**@author zzzi
             * @date 2024/3/7 16:41
             * 这里可以将执行了监听逻辑的监听器注销
             * 前提是监听器监听的事件后面不再会出现
             */
            listener.onApplicationEvent(event);
        }
    }
}
