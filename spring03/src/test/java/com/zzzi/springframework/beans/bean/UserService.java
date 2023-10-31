package com.zzzi.springframework.beans.bean;

public class UserService {

    private String name;
    private Integer age;

    public UserService(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public UserService() {
    }

    public UserService(String name) {
        this.name = name;
    }

    public void queryUserInfo() {
        System.out.println("查询用户信息：" + name+"  年龄："+age);
    }

    @Override
    public String toString() {
        return "UserService{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
