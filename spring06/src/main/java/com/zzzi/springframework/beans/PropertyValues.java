package com.zzzi.springframework.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zzzi
 * @date 2023/11/1 13:38
 * 在这里保存一个bean的所有依赖的属性
 * 每一个属性都是一个PropertyValue，保存到一个List<PropertyValue>数组中
 */
public class PropertyValues {
    private final List<PropertyValue> propertyValueList = new ArrayList<>();

    //添加一个PropertyValue属性
    public void addPropertyValue(PropertyValue propertyValue) {
        propertyValueList.add(propertyValue);
    }

    //获取所有的PropertyValue属性
    public PropertyValue[] getPropertyValues() {
        return propertyValueList.toArray(new PropertyValue[0]);
    }

    //根据依赖的属性名获取属性
    public PropertyValue getPropertyValue(String propertyName) {
        for (PropertyValue propertyValue : propertyValueList) {
            if (propertyValue.getName().equals(propertyName))
                return propertyValue;
        }
        return null;
    }

}
