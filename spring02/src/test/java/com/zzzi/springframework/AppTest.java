package com.zzzi.springframework;

import static org.junit.Assert.assertTrue;

import com.zzzi.springframework.bean.UserService;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.junit.Test;

/**
 * @author zzzi
 * @date 2023/10/30 13:27
 * 在这里对定义的IOC容器进行测试
 */
public class AppTest {

    @Test
    public void testBeanFactory() {
        //1.初始化BeanFactory
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        //2.保存要被管理的bean的类信息，需要手动注册
        BeanDefinition beanDefinition = new BeanDefinition(UserService.class);

        //3.保存当前bean与其类信息的映射
        beanFactory.registerBeanDefinition("userService",beanDefinition);

        //4.尝试从IOC容器中获取对应的bean，并且转型，这是简单工厂的设计模式
        //内部尝试获取bean对象，没有就会新建一个bean对象保存到IOC容器中
        UserService bean = (UserService) beanFactory.getBean("userService");
        bean.print();//print方法被调用

        //5.尝试再次获取bean对象，看是不是单例模式
        UserService beanSingleton= (UserService) beanFactory.getBean("userService");

        //6.测试是否是单例模式，直接比较两个bean对象的地址
        System.out.println(bean==beanSingleton);//true
    }
}
