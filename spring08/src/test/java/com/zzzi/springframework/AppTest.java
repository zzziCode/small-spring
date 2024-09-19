package com.zzzi.springframework;

import static org.junit.Assert.assertTrue;

import com.zzzi.springframework.bean.UserService;
import com.zzzi.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.Test;

/**
 * @author zzzi
 * @date 2023/11/6 15:58
 * 在这里测试容器资源是否注入成功
 * 整体分为两部分：
 * 1. initializeBean中注入直接可以注入的容器资源
 * 2. BeanPostProcessor中注入不能直接注入的资源（applicationContext），
 *    在可以访问的地方保存，在后面的实例化修改逻辑的Before中注入
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
