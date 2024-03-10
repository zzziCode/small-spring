package com.zzzi.springframework;

import static org.junit.Assert.assertTrue;

import com.zzzi.springframework.bean.IUserService;
import com.zzzi.springframework.bean.UserService;
import com.zzzi.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.Test;

/**
 * @author zzzi
 * @date 2023/11/12 14:39
 * 在这里测试自动注册和占位符替换的内容
 */
public class AppTest {
    /**
     * @author zzzi
     * @date 2023/11/12 14:39
     * 测试占位符替换
     * 这个配置文件中没有配置包扫描路径，所以执行的是正常的配置文件扫描过程
     */
    @Test
    public void testPlaceHolder() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-property.xml");
        UserService userService = applicationContext.getBean("userService", UserService.class);
        System.out.println(userService);
    }

    /**
     * @author zzzi
     * @date 2023/11/12 15:09
     * 测试自动注册模块
     * 这里只配置了包扫描路径的属性，所以会触发包扫描路径的功能
     */
    @Test
    public void testAutoScan() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-scan.xml");
        UserService userService = applicationContext.getBean("userService", UserService.class);
        System.out.println(userService.queryInfo());
        //存在的一个问题是：自动注册并没有将属性注入就直接创建了BeanDefinition，所以属性为空
        System.out.println(userService);
    }
}
