package com.zzzi.springframework.beans.factory.config;

/**
 * @author zzzi
 * @date 2023/10/31 19:07
 * 在这里保存bean依赖的单个属性的名称和值
 * 如果是普通属性直接保存值
 * 如果属性是一个bean，就保存其引用，其实就是一个BeanReference的对象
 * 内部存储了bean的姓名
 */
public class PropertyValue {
    //属性的名称和值不需要修改，使用final修饰
    private final String name;
    private final Object value;

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public PropertyValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }
}
