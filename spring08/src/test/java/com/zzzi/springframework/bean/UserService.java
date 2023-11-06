package com.zzzi.springframework.bean;


import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.BeanClassLoaderAware;
import com.zzzi.springframework.beans.factory.BeanFactory;
import com.zzzi.springframework.beans.factory.BeanFactoryAware;
import com.zzzi.springframework.beans.factory.BeanNameAware;
import com.zzzi.springframework.context.ApplicationContext;
import com.zzzi.springframework.context.ApplicationContextAware;

/**
 * @author zzzi
 * @date 2023/11/6 15:57
 * 在这里想要使用什么资源，就实现什么接口：
 * 1. 实现对应的接口
 * 2. 提供对应的成员属性
 * 3. 重写对应的set方法
 */
public class UserService implements BeanNameAware, BeanClassLoaderAware, ApplicationContextAware, BeanFactoryAware {

    private ApplicationContext applicationContext;
    private BeanFactory beanFactory;
    private ClassLoader classLoader;
    private String beanName;

    private String uId;
    private String company;
    private String location;
    private UserDao userDao;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        System.out.println("Bean Name is：" + name);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        System.out.println("ClassLoader：" + classLoader);
    }

    public String queryUserInfo() {
        return userDao.queryUserName(uId) + "," + company + "," + location;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }


    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    @Override
    public String toString() {
        return "UserService{" +
                "\n applicationContext=" + applicationContext +
                "\n beanFactory=" + beanFactory +
                "\n classLoader=" + classLoader +
                "\n beanName='" + beanName + '\'' +
                "\n uId='" + uId + '\'' +
                "\n company='" + company + '\'' +
                "\n location='" + location + '\'' +
                "\n userDao=" + userDao +
                "\n }";
    }
}
