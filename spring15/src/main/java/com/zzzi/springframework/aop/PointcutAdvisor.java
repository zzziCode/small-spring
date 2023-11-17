package com.zzzi.springframework.aop;
/**@author zzzi
 * @date 2023/11/11 15:47
 *在这里提供获取注册信息中匹配器的方法
 */
public interface PointcutAdvisor extends Advisor {
    Pointcut getPointcut();
}
