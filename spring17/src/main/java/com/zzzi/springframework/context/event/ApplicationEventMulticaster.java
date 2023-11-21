package com.zzzi.springframework.context.event;

import com.zzzi.springframework.context.ApplicationEvent;
import com.zzzi.springframework.context.ApplicationListener;

/**@author zzzi
 * @date 2023/11/8 13:22
 * 广播器接口，是事件和监听器之间的纽带，核心方法是multicastEvent
 */
public interface ApplicationEventMulticaster {
    void addApplicationListener(ApplicationListener<?> listener);

    void removeApplicationListener(ApplicationListener<?> listener);

    void multicastEvent(ApplicationEvent event);
}
