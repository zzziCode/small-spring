package com.zzzi.springframework.bean;

import com.zzzi.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

public class WifeAdvice implements MethodBeforeAdvice {
    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("给妻子加油打气(切面)" + method);
    }
}
