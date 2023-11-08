package com.zzzi.springframework.event;

import com.zzzi.springframework.context.ApplicationListener;
import com.zzzi.springframework.context.event.ContextClosedEvent;
/**@author zzzi
 * @date 2023/11/8 14:11
 * 容器关闭事件的监听器
 */
public class ContextClosedEventListener implements ApplicationListener<ContextClosedEvent> {
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        System.out.println("容器关闭事件的监听器执行：" + this.getClass().getName());
    }
}
