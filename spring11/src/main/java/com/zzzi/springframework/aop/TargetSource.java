package com.zzzi.springframework.aop;

/**
 * @author zzzi
 * @date 2023/11/11 15:53
 * 在这里保存要被代理的目标对象，并提供获取目标对象信息的方法
 */
public class TargetSource {
    private final Object target;

    public TargetSource(Object target) {
        this.target = target;
    }

    //获取被代理的目标对象
    public Object getTarget() {
        return target;
    }

    //获取被代理的目标对象所实现的所有接口
    public Class<?>[] getTargetClass() {
        return this.target.getClass().getInterfaces();
    }
}
