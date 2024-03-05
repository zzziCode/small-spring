package com.zzzi.springframework;

import static org.junit.Assert.assertTrue;

import com.zzzi.springframework.bean.UserService;
import com.zzzi.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.Test;

/**
 * @author zzzi
 * @date 2023/11/6 15:58
 * 在这里测试容器资源是否注入成功
 */
public class AppTest {

    @Test
    public void testResourcesInject() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
        applicationContext.registerShutdownHook();

        UserService userService = applicationContext.getBean("userService", UserService.class);
        String res = userService.queryUserInfo();
        System.out.println("查询结果为：" + res);

        System.out.println(userService);

    }
}
