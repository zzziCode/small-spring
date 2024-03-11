package com.zzzi.springframework;

import com.zzzi.springframework.bean2.IUserService;
import com.zzzi.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.Test;

public class ApiTest2 {

    /**@author zzzi
     * @date 2024/3/11 15:34
     * 测试包扫描统一配置+注解属性填充如何引入AOP
     */
    @Test
    public void test_autoProxy_2() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring2.xml");
        IUserService userService = applicationContext.getBean("userService", IUserService.class);
        System.out.println("测试结果：" + userService.queryUserInfo());
        System.out.println("动态代理之后的userService为：" + userService + "\n " + userService.getClass());
    }

}
