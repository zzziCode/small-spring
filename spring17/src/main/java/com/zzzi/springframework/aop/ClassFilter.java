package com.zzzi.springframework.aop;

/**
 * @author zzzi
 * @date 2023/11/11 14:39
 * 在这里定义类匹配器
 */
public interface ClassFilter {
    boolean matches(Class<?> clazz);
}
