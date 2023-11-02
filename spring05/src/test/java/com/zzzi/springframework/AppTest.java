package com.zzzi.springframework;

import static org.junit.Assert.assertTrue;

import com.zzzi.springframework.bean.UserService;
import com.zzzi.springframework.beans.factory.support.DefaultListableBeanFactory;
import com.zzzi.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import com.zzzi.springframework.core.io.FileSystemResource;
import org.junit.Test;

import java.io.File;

/**
 * @author zzzi
 * @date 2023/11/1 15:23
 * 在这里测试新增的从配置文件中读取信息的模块
 * 这个模块在获取bean对象之前调用
 */
public class AppTest {
    @Test
    public void testResourceXml() {
        // 1.初始化 BeanFactory
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        // 2. 读取配置文件&注册Bean
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        //读取配置文件
        //第一种方式：ClassPath
        reader.loadBeanDefinitions("classpath:spring.xml");
        //第二种方式：系统文件
        FileSystemResource fileSystemResource=new FileSystemResource(new File("G:\\Java\\spring\\spring05\\src\\test\\resources\\spring.xml"));
        //reader.loadBeanDefinitions(fileSystemResource);


        // 3. 获取Bean对象调用方法
        UserService userService = beanFactory.getBean("userService", UserService.class);
        String result = userService.query();
        System.out.println("测试结果：" + result);
    }
}
