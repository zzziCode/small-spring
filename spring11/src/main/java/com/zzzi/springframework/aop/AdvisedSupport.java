package com.zzzi.springframework.aop;

import org.aopalliance.intercept.MethodInterceptor;

/**
 * @author zzzi
 * @date 2023/11/11 15:57
 * 在这里封装创建代理对象所需要的所有参数
 * 之后将其传递给CGlib或者JDK的代理对象创建逻辑就可以创建对应的代理对象
 */
public class AdvisedSupport {
    //用来决定使用什么策略创建代理对象，默认使用JDK创建动态代理对象
    private boolean proxyTargetClass = false;
    //内部包装了通知和原始方法的执行顺序
    private MethodInterceptor methodInterceptor;
    //关于方法如何匹配
    private MethodMatcher methodMatcher;
    //被代理的目标对象
    private TargetSource targetSource;

    public TargetSource getTargetSource() {
        return targetSource;
    }

    public void setTargetSource(TargetSource targetSource) {
        this.targetSource = targetSource;
    }

    public boolean isProxyTargetClass() {
        return proxyTargetClass;
    }

    public void setProxyTargetClass(boolean proxyTargetClass) {
        this.proxyTargetClass = proxyTargetClass;
    }

    public MethodInterceptor getMethodInterceptor() {
        return methodInterceptor;
    }

    public void setMethodInterceptor(MethodInterceptor methodInterceptor) {
        this.methodInterceptor = methodInterceptor;
    }

    public MethodMatcher getMethodMatcher() {
        return methodMatcher;
    }

    public void setMethodMatcher(MethodMatcher methodMatcher) {
        this.methodMatcher = methodMatcher;
    }
}
