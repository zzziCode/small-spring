package com.zzzi.springframework.beans.factory;

import static org.junit.Assert.assertTrue;

import com.zzzi.springframework.beans.factory.bean.UserDao;
import com.zzzi.springframework.beans.factory.bean.UserService;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.BeanReference;
import com.zzzi.springframework.beans.factory.config.PropertyValue;
import com.zzzi.springframework.beans.factory.config.PropertyValues;
import com.zzzi.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.junit.Test;

/**
 * @author zzzi
 * @date 2023/10/31 20:09
 * 在这里完成对项目的测试
 * 主要是看bean的定义和属性填充是不是分离开了
 */
public class AppTest {
    @Test
    public void testBeanFactory() {
        //获取bean工厂
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();

        //注册UserDao的信息，因为它也是一个bean
        //有两种类型的beanDefinition，一种是直接存储类，另外一种还要存储属性列表
        BeanDefinition userDaoDefinition = new BeanDefinition(UserDao.class);
        //将注册信息保存到注册表中
        factory.registerBeanDefinition("userDao", userDaoDefinition);

        /**@author zzzi
         * @date 2023/10/31 20:45
         * 保存UserService的参数列表，便于后期属性填充，这一步正常情况下应该配置在xml文件中
         * 然后读取xml配置文件自动得到属性列表
         */
        PropertyValues propertyValues = new PropertyValues();
        //普通属性直接填充值，bean属性需要使用BeanReference来标记
        propertyValues.addPropertyValue(new PropertyValue("id", 2));
        //这一句代表当前的bean内部有个名为userDao的属性，其本身是一个bean，名称叫做userDao
        propertyValues.addPropertyValue(new PropertyValue("userDao", new BeanReference("userDao")));

        //注册UserService的信息
        BeanDefinition userServiceDefinition = new BeanDefinition(UserService.class, propertyValues);
        //将注册信息保存到注册表中
        factory.registerBeanDefinition("userService", userServiceDefinition);


        //获取对应的bean
        UserService userService = (UserService) factory.getBean("userService");
        userService.query();
    }
}
