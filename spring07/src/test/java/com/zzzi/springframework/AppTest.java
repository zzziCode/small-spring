package com.zzzi.springframework;

import static org.junit.Assert.assertTrue;

import com.zzzi.springframework.bean.UserDao;
import com.zzzi.springframework.bean.UserService;
import com.zzzi.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author zzzi
 * @date 2023/11/4 15:47
 * 在这里编写测试方法，测试在应用上下文中增加的初始化和销毁模块是否好用
 */
public class AppTest {
    @Test
    public void testXml() {
        // 1.初始化 BeanFactory，传入这个xml文件可以测试实例化后的两个方法与初始化方法的执行先后顺序
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:springPostProcessor.xml");
        //2. 注册钩子函数，容器关闭时触发，此时会触发bean的销毁逻辑
        applicationContext.registerShutdownHook();

        // 3. 获取Bean对象调用方法
        UserService userService = applicationContext.getBean("userService", UserService.class);
        String result = userService.queryUserInfo();
        System.out.println("测试结果：" + result);

    }
}
