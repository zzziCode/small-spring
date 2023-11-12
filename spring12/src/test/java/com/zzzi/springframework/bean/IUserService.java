package com.zzzi.springframework.bean;
/**@author zzzi
 * @date 2023/11/12 14:37
 * UserService要实现的接口
 */
public interface IUserService {
    String queryInfo();

    String register(String name);
}
