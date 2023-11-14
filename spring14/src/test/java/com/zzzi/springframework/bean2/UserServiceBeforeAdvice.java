package com.zzzi.springframework.bean2;
import com.zzzi.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
/**@author zzzi
 * @date 2023/11/14 14:46
 * 在这里定义一个前置通知
 */
public class UserServiceBeforeAdvice implements MethodBeforeAdvice {

    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("拦截方法：" + method.getName());
    }

}
