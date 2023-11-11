package com.zzzi.springframework.bean;

import com.zzzi.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
/**@author zzzi
 * @date 2023/11/11 15:42
 * 在这里定义一个前置通知，只有这个需要自己定义，之后就是在xml文件中进行配置
 */
public class UserServiceBeforeAdvice implements MethodBeforeAdvice {
    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("拦截到了方法：" + method.getName());
    }
}
