package com.zzzi.springframework.aop;

import org.aopalliance.aop.Advice;

/**
 * @author zzzi
 * @date 2023/11/11 15:46
 * 在这里提供获取注册信息中通知的方法
 */
public interface Advisor {
    Advice getAdvice();
}
