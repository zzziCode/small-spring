package com.zzzi.springframework.aop.framework;

import com.zzzi.springframework.aop.AdvisedSupport;

/**@author zzzi
 * @date 2023/11/11 16:37
 * 代理工厂的类，在这里可以根据不同的状态选择不同的创建逻辑得到代理对象
 */
public class ProxyFactory {
    private AdvisedSupport advisedSupport;

    public ProxyFactory(AdvisedSupport advisedSupport) {
        this.advisedSupport = advisedSupport;
    }
    public Object getProxy(){
        //1. 得到创建策略的对象
        AopProxy proxy = createProxy();
        //2. 创建代理对象
        Object proxyObject = proxy.getProxy();
        //3. 返回
        return proxyObject;
    }

    /**@author zzzi
     * @date 2023/12/12 13:57
     * 创建的代理对象内部保存了一个advisedSupport中的targetSource属性
     * targetSource属性内部保存了一个target，这是最终被代理的对象
     */
    private AopProxy createProxy() {
        if(advisedSupport.isProxyTargetClass()){
            return new Cglib2AopProxy(advisedSupport);
        }
        return new JdkDynamicAopProxy(advisedSupport);
    }
}
