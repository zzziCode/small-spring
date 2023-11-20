package com.zzzi.springframework.aop;

import java.lang.reflect.Method;

/**
 * @author zzzi
 * @date 2023/11/11 15:38
 * 在这里提供前置通知的待实现接口
 */
public interface MethodBeforeAdvice extends BeforeAdvice {
    void before(Method method, Object[] args, Object target) throws Throwable;
}
