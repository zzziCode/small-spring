package com.zzzi.springframework.beans;
/**@author zzzi
 * @date 2023/11/1 13:36
 * 在这里保存bean所依赖的单个属性的姓名和值
 */
public class PropertyValue {
    private final String name;
    private final Object value;

    public PropertyValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
