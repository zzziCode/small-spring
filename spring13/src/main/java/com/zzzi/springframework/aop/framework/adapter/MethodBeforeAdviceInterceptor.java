package com.zzzi.springframework.aop.framework.adapter;

import com.zzzi.springframework.aop.MethodBeforeAdvice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**@author zzzi
 * @date 2023/11/11 15:42
 * 在这里决定通知和原始方法的执行步骤
 */
public class MethodBeforeAdviceInterceptor implements MethodInterceptor {
    //保存自定义的前置通知
    private MethodBeforeAdvice advice;

    public MethodBeforeAdviceInterceptor() {
    }

    public MethodBeforeAdviceInterceptor(MethodBeforeAdvice advice) {
        this.advice = advice;
    }

    /**@author zzzi
     * @date 2023/11/11 15:44
     * 在这里决定通知和原始方法的执行顺序
     */
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        advice.before(methodInvocation.getMethod(),methodInvocation.getArguments(),methodInvocation.getThis());
        return methodInvocation.proceed();
    }
}
