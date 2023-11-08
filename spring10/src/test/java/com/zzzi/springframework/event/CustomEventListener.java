package com.zzzi.springframework.event;

import com.zzzi.springframework.context.ApplicationListener;

import java.util.Date;
/**@author zzzi
 * @date 2023/11/8 14:14
 * 自定义事件的监听器
 */
public class CustomEventListener implements ApplicationListener<CustomEvent> {
    @Override
    public void onApplicationEvent(CustomEvent event) {
        System.out.println("自定义事件的监听器执行");
        System.out.println("收到：" + event.getSource() + "的消息;时间：" + new Date());
        System.out.println("消息为：" + event.getId() + ",\t" + event.getMessage());
    }
}
