package com.zzzi.springframework;

import static org.junit.Assert.assertTrue;

import com.zzzi.springframework.aop.AdvisedSupport;
import com.zzzi.springframework.aop.ClassFilter;
import com.zzzi.springframework.aop.TargetSource;
import com.zzzi.springframework.aop.aspectj.AspectJExpressionPointcut;
import com.zzzi.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import com.zzzi.springframework.aop.framework.ProxyFactory;
import com.zzzi.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor;
import com.zzzi.springframework.bean.IUserService;
import com.zzzi.springframework.bean.UserService;
import com.zzzi.springframework.bean.UserServiceBeforeAdvice;
import com.zzzi.springframework.bean.UserServiceInterceptor;
import com.zzzi.springframework.context.support.ClassPathXmlApplicationContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.Before;
import org.junit.Test;

/**
 * @author zzzi
 * @date 2023/11/11 17:04
 * 在这里进行测试
 */
public class AppTest {
    private AdvisedSupport advisedSupport;

    /**
     * @author zzzi
     * @date 2023/11/11 19:19
     * 在每个单元测试之前都执行一次
     * 用来初始化代理对象创建的参数
     */
    @Before
    public void init() {
        // 目标对象
        IUserService userService = new UserService();
        // 组装代理信息
        advisedSupport = new AdvisedSupport();
        advisedSupport.setTargetSource(new TargetSource(userService));
        advisedSupport.setMethodInterceptor(new UserServiceInterceptor());
        advisedSupport.setMethodMatcher(new AspectJExpressionPointcut("execution(* com.zzzi.springframework.bean.IUserService.*(..))"));
    }

    /**
     * @author zzzi
     * @date 2023/11/11 19:19
     * 测试代理工厂是否有效
     */
    @Test
    public void testProxyFactory() {
        advisedSupport.setProxyTargetClass(false);
        ProxyFactory proxyFactory = new ProxyFactory(advisedSupport);
        //根据配置的advisedSupport创建一个代理对象，查看对象是否被增强
        IUserService userService = (IUserService) proxyFactory.getProxy();
        System.out.println(userService.queryUserInfo());
    }

    /**
     * @author zzzi
     * @date 2023/11/11 19:24
     * 测试方法拦截器是否有效
     */
    @Test
    public void testMethodInterceptor() {
        //创建新的方法拦截器
        UserServiceBeforeAdvice beforeAdvice = new UserServiceBeforeAdvice();
        MethodBeforeAdviceInterceptor interceptor = new MethodBeforeAdviceInterceptor(beforeAdvice);
        //更新方法拦截器
        advisedSupport.setMethodInterceptor(interceptor);
        //根据配置好的advisedSupport来创建代理工厂
        ProxyFactory proxyFactory = new ProxyFactory(advisedSupport);
        //创建得到代理对象
        IUserService userService = (IUserService) proxyFactory.getProxy();
        System.out.println(userService.queryUserInfo());
    }

    /**@author zzzi
     * @date 2023/11/11 19:27
     * 测试代理信息封装模块是否有效
     */
    @Test
    public void testAspectJExpressionPointcutAdvisor(){
        //要代理的目标对象
        UserService userService=new UserService();

        //创建一个代理信息封装模块
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        //封装切入点表达式
        advisor.setExpression("execution(* com.zzzi.springframework.bean.IUserService.*(..))");
        //封装拦截器
        advisor.setAdvice(new MethodBeforeAdviceInterceptor(new UserServiceBeforeAdvice()));

        //执行类的匹配
        ClassFilter classFilter = advisor.getPointcut().getClassFilter();
        //匹配成功需要创建代理对象
        if(classFilter.matches(userService.getClass())){
            AdvisedSupport advisedSupport=new AdvisedSupport();

            //封装创建代理对象需要的信息
            //封装目标对象
            advisedSupport.setTargetSource(new TargetSource(userService));
            //封装一个选择什么代理创建策略的状态
            advisedSupport.setProxyTargetClass(false);
            //封装一个方法匹配器
            advisedSupport.setMethodMatcher(advisor.getPointcut().getMethodMatcher());
            //封装一个方法拦截器
            advisedSupport.setMethodInterceptor((MethodInterceptor) advisor.getAdvice());

            //根据封装的信息创建得到代理对象
            ProxyFactory proxyFactory = new ProxyFactory(advisedSupport);
            IUserService userServiceProxy = (IUserService) proxyFactory.getProxy();

            System.out.println(userServiceProxy.queryUserInfo());
        }

    }

    /**
     * @author zzzi
     * @date 2023/11/11 17:04
     * 测试AOP是否引入成功
     */
    @Test
    public void testAop() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
        IUserService userService = applicationContext.getBean("userService", IUserService.class);
        //发现执行时已经增强过了
        System.out.println(userService.queryUserInfo());
    }
}
