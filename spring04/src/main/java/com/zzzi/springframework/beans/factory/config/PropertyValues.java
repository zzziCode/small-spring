package com.zzzi.springframework.beans.factory.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zzzi
 * @date 2023/10/31 19:10
 * 在这里保存一个bean的所有属性，每一个属性都成为一个PropertyValue
 */
public class PropertyValues {
    private final List<PropertyValue> propertyValueList = new ArrayList<>();

    //向其中添加一个属性对
    public void addPropertyValue(PropertyValue propertyValue) {
        propertyValueList.add(propertyValue);
    }

    //获取指定的属性对
    public PropertyValue getPropertyValue(String name) {
        //遍历当前容器，找到指定的PropertyValue
        for (PropertyValue propertyValue : propertyValueList) {
            if (propertyValue.getName().equals(name)) {
                return propertyValue;
            }
        }
        return null;
    }

    //获取所有属性对
    public PropertyValue[] getPropertyValues() {
        //直接将List中的元素放到一个指定的数组中返回，而不是返回一个List
        return propertyValueList.toArray(new PropertyValue[propertyValueList.size()]);
    }
}
