package com.zzzi.springframework.context;

/**
 * @author zzzi
 * @date 2023/11/8 13:17
 * 所有的事件都要从这个接口中的方法发布出去
 * 而项目中核心的东西就是应用上下文
 * 所以这个接口应该注入给应用上下文
 * 使用应用上下文调用其接口发布事件
 */
public interface ApplicationEventPublisher {
    void publishEvent(ApplicationEvent event);
}
