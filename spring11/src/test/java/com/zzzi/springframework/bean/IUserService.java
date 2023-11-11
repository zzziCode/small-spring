package com.zzzi.springframework.bean;

/**
 * @author zzzi
 * @date 2023/11/11 17:01
 * UserService实现的接口
 */
public interface IUserService {
    String queryUserInfo();

    String register(String userName);
}
