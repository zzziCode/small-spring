package com.zzzi.springframework.bean;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;


/**
 * @author zzzi
 * @date 2023/11/11 19:19
 * 在这里自定义方法的拦截器，通知和原始方法 的执行顺序
 * 正常来说应该是将通知封装到对应的方法拦截器中，这里直接编写方法拦截器，没有通知
 */
public class UserServiceInterceptor implements MethodInterceptor {


    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return methodInvocation.proceed();
        } finally {
            System.out.println("监控 - Begin By AOP");
            System.out.println("方法名称：" + methodInvocation.getMethod());
            System.out.println("方法耗时：" + (System.currentTimeMillis() - start) + "ms");
            System.out.println("监控 - End\r\n");
        }
    }
}
