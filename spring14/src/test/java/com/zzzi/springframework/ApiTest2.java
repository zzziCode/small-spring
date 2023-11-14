package com.zzzi.springframework;

import com.zzzi.springframework.bean2.IUserService;
import com.zzzi.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.Test;

public class ApiTest2 {

    @Test
    public void test_autoProxy_2() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring2.xml");
        IUserService userService = applicationContext.getBean("userService", IUserService.class);
        System.out.println("测试结果：" + userService.queryUserInfo());
        System.out.println("动态代理之后的userService为：" + userService + "\n " + userService.getClass());
    }

}
