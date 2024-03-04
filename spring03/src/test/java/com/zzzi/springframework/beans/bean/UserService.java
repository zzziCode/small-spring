package com.zzzi.springframework.beans.bean;
/**@author zzzi
 * @date 2024/1/7 21:06
 * 这个类要有这种类型的构造函数才能创建对应的bean对象
 * 参数列表不对，无法创建对应的bean对象
 */
public class UserService {

    private String name;
    private Integer age;

    //提供三种不同的构造函数，从而使得可以创建不同种类的带参bean
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
