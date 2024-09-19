package com.zzzi.springframework;

import static org.junit.Assert.assertTrue;

import com.zzzi.springframework.bean.UserService;
import com.zzzi.springframework.beans.factory.support.DefaultListableBeanFactory;
import com.zzzi.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import com.zzzi.springframework.core.io.FileSystemResource;
import com.zzzi.springframework.core.io.UrlResource;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author zzzi
 * @date 2023/11/1 15:23
 * 在这里测试新增的从配置文件中读取信息的模块
 * 这个模块在获取bean对象之前调用
 * 这里还是手动加载配置文件中的信息，后期拥有应用上下文功能之后，这些工作都变成自动
 */
public class AppTest {
    @Test
    public void testResourceXml() throws MalformedURLException {
        // 1.初始化 BeanFactory，后期从配置文件中读取到的bean定义信息需要保存到这里
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        // 2. 读取配置文件&注册Bean
        // 这里将beanFactory传入是为了将从配置文件中读取到的BeanDefinition保存到beanFactory的Map中
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        //读取配置文件
        //第一种方式：ClassPath
        //reader.loadBeanDefinitions("classpath:spring.xml");
        //第二种方式：系统文件（传递路径或者直接传递资源对象都可）
        //FileSystemResource fileSystemResource=new FileSystemResource(new File("G:\\Java\\spring\\spring05\\src\\test\\resources\\spring.xml"));
        //reader.loadBeanDefinitions("G:\\Java\\spring\\spring05\\src\\test\\resources\\spring.xml");
        ////这一个loadBeanDefinitions就可以将配置文件中的信息读取并保存到beanFactory中的beanDefinitionMap中

        // 3. 使用远程配置文件
        URL url = new URL("http://106.75.247.170:9000/test/spring.xml");
        UrlResource urlResource = new UrlResource(url);
        reader.loadBeanDefinitions(urlResource);

        // 4. 获取Bean对象调用方法
        UserService userService = beanFactory.getBean("userService", UserService.class);
        String result = userService.query();
        System.out.println("测试结果：" + result);

    }
}
