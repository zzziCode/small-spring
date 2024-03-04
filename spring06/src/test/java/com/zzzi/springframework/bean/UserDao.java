package com.zzzi.springframework.bean;

import java.util.HashMap;
import java.util.Map;

public class UserDao {

    public static Map<String, String> hashMap = new HashMap<>();

    @Override
    public String toString() {
        return hashMap.toString();
    }

    static {
        hashMap.put("1", "张三");
        hashMap.put("2", "李四");
        hashMap.put("3", "王五");
    }

    public String queryUserName(String uId) {
        return hashMap.get(uId);
    }

}
