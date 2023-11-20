package com.zzzi.springframework.aop.framework;

import com.zzzi.springframework.aop.AdvisedSupport;
import com.zzzi.springframework.aop.MethodMatcher;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**@author zzzi
 * @date 2023/11/11 16:23
 * 第一种创建代理对象的方式：JDK
 */
public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {
    //保存创建代理对象所需要的全部参数
    private final AdvisedSupport adviceSupport;

    public JdkDynamicAopProxy(AdvisedSupport adviceSupport) {
        this.adviceSupport = adviceSupport;
    }
    //返回创建的代理对象
    @Override
    public Object getProxy() {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),adviceSupport.getTargetSource().getTargetClass(),this);
    }
    /**@author zzzi
     * @date 2023/11/11 16:15 调用原始方法最终会到这里
     * 在这里确定代理对象中的方法有哪些需要被增强
     * 也就是那些与切入点表达式匹配成功
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //拿到当前的方法匹配器尝试匹配当前方法
        MethodMatcher matcher=adviceSupport.getMethodMatcher();
        //如果当前方法匹配到了
        if(matcher.matches(method,adviceSupport.getTargetSource().getTarget().getClass())){
            MethodInterceptor methodInterceptor = adviceSupport.getMethodInterceptor();
            //将被代理的方法传入进去，执行增强逻辑
            ReflectiveMethodInvocation invocation = new ReflectiveMethodInvocation(adviceSupport.getTargetSource().getTarget(), method, args);
            return methodInterceptor.invoke(invocation);
        }
        //没匹配上直接利用反射执行原始方法
        return method.invoke(adviceSupport.getTargetSource().getTarget(),args);
    }
}
