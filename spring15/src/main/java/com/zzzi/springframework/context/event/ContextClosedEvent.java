package com.zzzi.springframework.context.event;
/**@author zzzi
 * @date 2023/11/8 13:54
 * 容器关闭事件
 */
public class ContextClosedEvent extends ApplicationContextEvent{
    public ContextClosedEvent(Object source) {
        super(source);
    }

}
