package com.zzzi.springframework.beans.factory.bean;

import java.util.HashMap;
import java.util.Map;

public class UserDao {
    private static Map<Integer,String> map=new HashMap<>();

    static {
        map.put(1,"张三");
        map.put(2,"李四");
        map.put(3,"王五");
    }
    public String query(int id){
        return map.get(id);
    }
}
