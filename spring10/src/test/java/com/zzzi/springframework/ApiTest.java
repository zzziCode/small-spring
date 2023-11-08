package com.zzzi.springframework;

import com.zzzi.springframework.context.support.ClassPathXmlApplicationContext;
import com.zzzi.springframework.event.CustomEvent;
import org.junit.Test;

/**
 * @author zzzi
 * @date 2023/11/8 14:15
 * 在这里完成对事件机制的测试
 */
public class ApiTest {
    @Test
    public void testEvent() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
        applicationContext.publishEvent(new CustomEvent(this, 468, "测试自定义事件"));
        applicationContext.registerShutdownHook();

    }
}
