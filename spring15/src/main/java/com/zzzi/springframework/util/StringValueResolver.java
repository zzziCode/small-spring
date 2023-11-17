package com.zzzi.springframework.util;

/**
 * @author zzzi
 * @date 2023/11/13 15:18
 * 在这个类中提供一个方法，从而实现占位符的替换
 */
public interface StringValueResolver {
    String resolveStringValue(String strVal);
}
