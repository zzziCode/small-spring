package com.zzzi.springframework.bean;

import java.util.Random;
/**@author zzzi
 * @date 2023/11/11 19:38
 * 项目中的一个bean对象
 */
public class UserService implements IUserService{
    @Override
    public String queryUserInfo() {
        try {
            Thread.sleep(new Random(1).nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "queryUserInfo原始方法调用执行";
    }

    @Override
    public String register(String userName) {
        try {
            Thread.sleep(new Random(1).nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "注册用户：" + userName + " success！";
    }
}
