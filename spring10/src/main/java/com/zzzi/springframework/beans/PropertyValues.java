package com.zzzi.springframework.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zzzi
 * @date 2023/11/4 13:07
 * 在这里保存一个bean所有依赖的属性，最终被保存到BeanDefinition中
 */
public class PropertyValues {
    private final List<PropertyValue> propertyValueList = new ArrayList<>();

    //向容器中添加一个属性
    public void addPropertyValue(PropertyValue propertyValue) {
        propertyValueList.add(propertyValue);
    }

    //按名称拿到一个属性
    public PropertyValue getPropertyValue(String name) {
        for (PropertyValue propertyValue : propertyValueList) {
            if (propertyValue.getName().equals(name))
                return propertyValue;
        }
        return null;
    }

    //拿到一个数组，其中存储的所有属性
    public PropertyValue[] getPropertyValues() {
        return propertyValueList.toArray(new PropertyValue[0]);
    }
}
