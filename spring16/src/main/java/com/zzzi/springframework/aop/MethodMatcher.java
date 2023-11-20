package com.zzzi.springframework.aop;

import java.lang.reflect.Method;

/**@author zzzi
 * @date 2023/11/11 14:40
 * 在这里定义方法匹配器
 */
public interface MethodMatcher {
    boolean matches(Method method, Class<?> targetClass);
}
