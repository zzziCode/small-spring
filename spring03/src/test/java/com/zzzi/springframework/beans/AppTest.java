package com.zzzi.springframework.beans;

import static org.junit.Assert.assertTrue;

import com.zzzi.springframework.beans.bean.UserService;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * @author zzzi
 * @date 2023/10/31 14:57
 * 在这里进行测试
 */
public class AppTest {
    /**
     * @author zzzi
     * @date 2023/10/31 15:08
     * 在这里测试新的bean工厂是否能够创建带参的bean对象
     */
    @Test
    public void testBeanFactory() {
        // 1.初始化 BeanFactory
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        // 3. 注入bean,将bean的类信息注册到一个容器中保存
        BeanDefinition beanDefinition = new BeanDefinition(UserService.class);
        beanFactory.registerBeanDefinition("userService", beanDefinition);

        // 4.获取bean，此时可以传递参数，且三种长度的参数都可以接收，这是因为有对应的构造函数
        //获取bean对象时按照参数列表来区分
        UserService userService = (UserService) beanFactory.getBean("userService", "小张",13);
        userService.queryUserInfo();
        System.out.println(userService);

        //UserService userService = (UserService) beanFactory.getBean("userService", "小王");
        //userService.queryUserInfo();
        //System.out.println(userService);

        //UserService userService = (UserService) beanFactory.getBean("userService");
        //userService.queryUserInfo();
        //System.out.println(userService);
    }

    /**
     * @author zzzi
     * @date 2023/10/31 15:05
     * 在这里测试bean对象使用基本数据类型和包装类型为参数的区别
     * 使用基本类型无法匹配到正确的构造函数，因为传递13时内部的object数组会将13转换成Integer类型
     * 与构造函数参数列表中的int类型无法匹配
     */
    @Test
    public void testObjectParam() {
        Person person = new Person(13, "张三");
        //java底层做了转换，int转换成了Integer，String转换成了字符数组
        Object[] args = person.getArgs();
        Constructor<?>[] declaredConstructors = Person.class.getDeclaredConstructors();
        for (Constructor<?> declaredConstructor : declaredConstructors) {
            Class<?>[] parameterTypes = declaredConstructor.getParameterTypes();
            if (parameterTypes.length != args.length)
                continue;
            //参数个数一样，看类型是否一样
            int i = 0;
            for (; i < parameterTypes.length; i++) {
                //参数列表的类型不匹配,继续搜索合适的构造函数
                if (parameterTypes[i] != args[i].getClass())
                    break;
            }
            //参数列表匹配到了末尾说明找到目标构造函数
            if (i == parameterTypes.length) {
                System.out.println("匹配成功");
                break;
            }
        }
    }
}

class Person {
    private int age;
    private String name;
    Object[] args = new Object[2];

    public Person(int age, String name) {
        args[0] = age;
        args[1] = name;
        this.age = age;
        this.name = name;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Person() {
    }
}
