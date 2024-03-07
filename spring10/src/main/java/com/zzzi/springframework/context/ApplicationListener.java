package com.zzzi.springframework.context;

import java.util.EventListener;
/**@author zzzi
 * @date 2023/11/8 13:16
 * 所有的监听器都要实现这个接口
 * 监听器针对事件要执行什么逻辑都写到了onApplicationEvent方法中
 */
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

    void onApplicationEvent(E event);
}
