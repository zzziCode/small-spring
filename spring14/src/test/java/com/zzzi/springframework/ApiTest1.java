package com.zzzi.springframework;

import com.zzzi.springframework.bean1.IUserService;
import com.zzzi.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.Test;


public class ApiTest1 {

    @Test
    public void test_autoProxy() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring1.xml");
        IUserService userService = applicationContext.getBean("userService", IUserService.class);
        System.out.println("测试结果：" + userService.queryUserInfo());
        System.out.println("动态代理之后的userService为：" + userService + "\n " + userService.getClass());

    }

}
