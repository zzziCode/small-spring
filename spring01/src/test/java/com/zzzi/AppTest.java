package com.zzzi;

import static org.junit.Assert.assertTrue;

import com.zzzi.springframework.BeanDefinition;
import com.zzzi.springframework.BeanFactory;
import com.zzzi.springframework.bean.UserService;
import org.junit.Test;

/**
 * 在这里测试基础的IOC容器的注入和取出bean对象的功能
 */
public class AppTest {
    @Test
    public void testBeanFactory() {
        //1.定义
        //获取工厂对象
        BeanFactory beanFactory = new BeanFactory();
        //准备要注入的bean对象
        UserService userService = new UserService();
        //将其注入到BeanDefinition中
        BeanDefinition beanDefinition = new BeanDefinition(userService);
        //2.注册
        //将bean保存到IOC容器中
        beanFactory.
                registerBeanDefinition("userService", beanDefinition);
        //3.获取
        //根据名称从IOC容器中取出对应的bean
        UserService user = (UserService) beanFactory.getBean("userService");
        //执行bean的对应方法
        user.print();
    }
}
