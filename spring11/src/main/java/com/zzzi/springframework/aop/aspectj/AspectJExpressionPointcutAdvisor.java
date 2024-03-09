package com.zzzi.springframework.aop.aspectj;

import com.zzzi.springframework.aop.Pointcut;
import com.zzzi.springframework.aop.PointcutAdvisor;
import org.aopalliance.aop.Advice;

/**
 * @author zzzi
 * @date 2023/11/11 15:48
 * 在这里封装所有的配置信息，最终这个类会被注册成一个bean
 */
public class AspectJExpressionPointcutAdvisor implements PointcutAdvisor {
    //切面，保存匹配器
    private AspectJExpressionPointcut pointcut;
    //通知，保存具体的方法增强逻辑
    private Advice advice;
    //切入点表达式
    private String expression;


    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void setAdvice(Advice advice) {
        this.advice = advice;
    }

    @Override
    public Advice getAdvice() {
        return advice;
    }

    @Override
    public Pointcut getPointcut() {
        if (pointcut == null)
            //直接在内部新建一个匹配器即可
            return new AspectJExpressionPointcut(expression);
        return pointcut;
    }
}
