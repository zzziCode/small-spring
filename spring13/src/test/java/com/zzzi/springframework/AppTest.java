package com.zzzi.springframework;

import static org.junit.Assert.assertTrue;

import com.zzzi.springframework.bean.IUserService;
import com.zzzi.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.Test;

/**
 * @author zzzi
 * @date 2023/11/13 16:27
 * 在这里测试注解属性填充功能
 */
public class AppTest {

    @Test
    public void testAnnotation() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
        IUserService userService = applicationContext.getBean("userService", IUserService.class);
        System.out.println(userService.queryUserInfo());
    }
}
