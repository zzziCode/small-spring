package com.zzzi.springframework.context.event;

import com.zzzi.springframework.context.ApplicationContext;
import com.zzzi.springframework.context.ApplicationEvent;

/**
 * @author zzzi
 * @date 2023/11/8 13:20
 * 项目中所有的事件都要继承这个类
 */
public class ApplicationContextEvent extends ApplicationEvent {
    public ApplicationContextEvent(Object source) {
        super(source);
    }

    public final ApplicationContext getApplicationContext() {
        return (ApplicationContext) getSource();
    }
}
