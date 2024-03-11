package com.zzzi.springframework;

import com.zzzi.springframework.bean1.IUserService;
import com.zzzi.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.Test;


public class ApiTest1 {

    /**@author zzzi
     * @date 2024/3/11 15:33
     * 测试使用手动配置bean的方式引入AOP
     * 这里没使用包扫描路径，所以也不会进行注解属性配置的步骤
     */
    @Test
    public void test_autoProxy() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring1.xml");
        IUserService userService = applicationContext.getBean("userService", IUserService.class);
        System.out.println("测试结果：" + userService.queryUserInfo());
        System.out.println("动态代理之后的userService为：" + userService + "\n " + userService.getClass());

    }

}
