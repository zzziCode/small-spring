package com.zzzi.springframework.bean;

import com.zzzi.springframework.beans.factory.annotation.Autowired;
import com.zzzi.springframework.beans.factory.annotation.Value;
import com.zzzi.springframework.sterotype.Component;

import java.util.Random;

@Component
public class UserService implements IUserService {//在这里使用@Value注解，将token注入到UserService中
    @Value("${token}")
    private String token;
    //在这里使用@Autowired注解，将UserDao注入到UserService中
    @Autowired
    private UserDao userDao;

    public String queryUserInfo() {
        try {
            Thread.sleep(new Random(1).nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return userDao.queryUserName("1") + "，" + token;
    }

    public String register(String userName) {
        try {
            Thread.sleep(new Random(1).nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "注册用户：" + userName + " success！";
    }

    @Override
    public String toString() {
        return "UserService{" +
                "token='" + token + '\'' +
                ", userDao=" + userDao +
                '}';
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}
