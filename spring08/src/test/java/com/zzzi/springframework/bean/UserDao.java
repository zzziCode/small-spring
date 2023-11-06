package com.zzzi.springframework.bean;

import java.util.HashMap;
import java.util.Map;

/**@author zzzi
 * @date 2023/11/6 13:03
 * 通过xml配置的方式实现初始化和销毁操作
 */
public class UserDao{

    private static Map<String, String> hashMap = new HashMap<>();

    public void initDataMethod(){
        System.out.println("执行：init-method");
        hashMap.put("1", "张三");
        hashMap.put("2", "李四");
        hashMap.put("3", "王五");
    }

    public void destroyDataMethod(){
        System.out.println("执行：destroy-method");
        hashMap.clear();
    }

    public String queryUserName(String uId) {
        return hashMap.get(uId);
    }

    @Override
    public String toString() {
        return hashMap.toString();
    }
}
