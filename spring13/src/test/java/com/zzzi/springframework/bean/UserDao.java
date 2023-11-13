package com.zzzi.springframework.bean;

import com.zzzi.springframework.sterotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserDao {
    private static Map<String, String> hashMap = new HashMap<>();

    static {
        hashMap.put("1", "小傅哥，北京，亦庄");
        hashMap.put("2", "八杯水，上海，尖沙咀");
        hashMap.put("3", "阿毛，天津，东丽区");
    }

    public String queryUserName(String uId) {
        return hashMap.get(uId);
    }

    @Override
    public String toString() {
        return hashMap.toString();
    }
}
