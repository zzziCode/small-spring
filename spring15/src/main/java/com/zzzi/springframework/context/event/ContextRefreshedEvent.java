package com.zzzi.springframework.context.event;
/**@author zzzi
 * @date 2023/11/8 13:55
 * 容器刷新完成事件
 */
public class ContextRefreshedEvent extends ApplicationContextEvent{
    public ContextRefreshedEvent(Object source) {
        super(source);
    }
}
