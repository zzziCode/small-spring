package com.zzzi.springframework.bean;

/**@author zzzi
 * @date 2023/10/30 13:28
 * 想要交给IOC容器管理的bean
 */
public class UserService {
    public void print(){
        System.out.println("print方法被调用");
    }
}
