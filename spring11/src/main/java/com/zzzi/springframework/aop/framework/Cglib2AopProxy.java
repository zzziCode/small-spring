package com.zzzi.springframework.aop.framework;

import com.zzzi.springframework.aop.AdvisedSupport;
import com.zzzi.springframework.aop.MethodMatcher;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author zzzi
 * @date 2023/11/11 16:22
 * 第二种创建代理对象的方式：Cglib
 */
public class Cglib2AopProxy implements AopProxy {
    private final AdvisedSupport advisedSupport;

    public Cglib2AopProxy(AdvisedSupport adviceSupport) {
        this.advisedSupport = adviceSupport;
    }

    //对外提供统一接口返回创建得到的代理对象
    @Override
    public Object getProxy() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(advisedSupport.getTargetSource().getTarget().getClass());
        enhancer.setInterfaces(advisedSupport.getTargetSource().getTargetClass());
        enhancer.setCallback(new DynamicAdvisedInterceptor(advisedSupport));
        //创建一个代理对象返回，后期调用代理对象中的方法时，调用回调函数intercept中的逻辑
        return enhancer.create();
    }

    /**
     * @author zzzi
     * @date 2023/11/11 16:29
     * 设置Cglib的回调函数intercept，执行原始方法时会调用此函数
     * 在这个函数中实现通知执行的逻辑，也就是确定原始方法和通知方法的执行顺序
     */
    private static class DynamicAdvisedInterceptor implements MethodInterceptor {

        private final AdvisedSupport advised;

        public DynamicAdvisedInterceptor(AdvisedSupport advised) {
            this.advised = advised;
        }

        /**@author zzzi
         * @date 2024/3/9 13:44
         * 动态代理对象执行自己的方法时，会触发此方法的执行
         * 判断当前方法是否需要被增强，也就是是否与切入点表达式匹配
         */
        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            //得到新的原始方法执行逻辑和方法匹配器
            CglibMethodInvocation cglibMethodInvocation = new CglibMethodInvocation(advised.getTargetSource().getTarget(), method, objects, methodProxy);
            MethodMatcher methodMatcher = advised.getMethodMatcher();
            //进行方法的匹配
            if (methodMatcher.matches(method, advised.getTargetSource().getTarget().getClass())) {
                //匹配成功执行拦截器中规定的通知和原始方法的执行顺序
                return advised.getMethodInterceptor().invoke(cglibMethodInvocation);
            }
            //没匹配成功执行原始方法
            return cglibMethodInvocation.proceed();
        }
    }

    /**
     * @author zzzi
     * @date 2023/11/11 16:28
     * 在ReflectiveMethodInvocation类的基础上重新封装Cglib的原始方法执行的逻辑
     */
    private static class CglibMethodInvocation extends ReflectiveMethodInvocation {

        private final MethodProxy methodProxy;

        public CglibMethodInvocation(Object target, Method method, Object[] arguments, MethodProxy methodProxy) {
            super(target, method, arguments);
            //增加了一个参数
            this.methodProxy = methodProxy;
        }

        //Cglib中原始方法的执行逻辑
        @Override
        public Object proceed() throws Throwable {
            return this.methodProxy.invoke(this.target, this.args);
        }
    }
}
