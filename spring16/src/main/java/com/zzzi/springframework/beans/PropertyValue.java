package com.zzzi.springframework.beans;
/**@author zzzi
 * @date 2023/11/4 13:06
 * 在这里实现bean的单个属性的名和值的存储
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
