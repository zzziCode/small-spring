package com.zzzi.springframework.bean;

import com.zzzi.springframework.sterotype.Component;

import java.util.Random;
/**@author zzzi
 * @date 2023/11/12 14:40
 * 在这里完成自动的扫描注册
 */
@Component
public class UserService implements IUserService {
    String token;

    @Override
    public String queryInfo() {
        try {
            Thread.sleep(new Random(1).nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "小傅哥，100001，深圳";
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
                '}';
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
