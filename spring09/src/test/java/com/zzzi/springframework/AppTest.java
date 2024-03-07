package com.zzzi.springframework;

import static org.junit.Assert.assertTrue;

import com.zzzi.springframework.bean.UserService;
import com.zzzi.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.Test;
import org.openjdk.jol.info.ClassLayout;

/**
 * @author zzzi
 * @date 2023/11/7 10:18
 * 在这里编写测试类
 */
public class AppTest {
    //测试单例和原型模式是否生效
    @Test
    public void testPrototype() {
        // 1.初始化 BeanFactory
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
        applicationContext.registerShutdownHook();

        // 2. 获取Bean对象调用方法
        UserService userService01 = applicationContext.getBean("userService", UserService.class);
        UserService userService02 = applicationContext.getBean("userService", UserService.class);

        // 3. 配置 scope="prototype/singleton"，查看单例和原型之间的区别
        System.out.println(userService01);
        System.out.println(userService02);
        System.out.println("两个bean是否相等：" + (userService01 == userService02));

    }
    //测试新的bean创建方式是否生效
    /**@author zzzi
     * @date 2024/3/7 15:05
     * 获取时先从ioc容器中获取外壳bean
     * 之后从外壳bean中的缓存factoryBeanObjectCache或者getObject方法中获取真正的bean
     */
    @Test
    public void testFactoryBean() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
        applicationContext.registerShutdownHook();
        UserService userService = applicationContext.getBean("userService", UserService.class);

        String res = userService.queryUserInfo();
        System.out.println(res);
        System.out.println(userService);

    }
}
