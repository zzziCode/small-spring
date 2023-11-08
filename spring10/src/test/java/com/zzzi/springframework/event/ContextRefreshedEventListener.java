package com.zzzi.springframework.event;

import com.zzzi.springframework.context.ApplicationListener;
import com.zzzi.springframework.context.event.ContextRefreshedEvent;
/**@author zzzi
 * @date 2023/11/8 14:12
 * 容器刷新完成的监听器
 */
public class ContextRefreshedEventListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("容器刷新完成事件的监听器执行：" + this.getClass().getName());
    }
}
