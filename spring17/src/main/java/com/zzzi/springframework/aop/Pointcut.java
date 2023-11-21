package com.zzzi.springframework.aop;

/**
 * @author zzzi
 * @date 2023/11/11 14:38
 * 在这里获取两个匹配器
 */
public interface Pointcut {
    ClassFilter getClassFilter();

    MethodMatcher getMethodMatcher();
}
