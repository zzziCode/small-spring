package com.zzzi.springframework.bean;

import com.zzzi.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
/**@author zzzi
 * @date 2023/11/7 11:12
 * 在这个内部创建真正的bean
 */
public class ProxyBeanFactory implements FactoryBean<IUserDao> {
    //将userDao的创建移动到这个方法中
    @Override
    public IUserDao getObject() throws Exception {
        //动态代理中的handler
        InvocationHandler handler = (proxy, method, args) -> {

            // 添加排除方法，toString方法不代理
            if ("toString".equals(method.getName())) return this.toString();
            //到这里就是剩下的queryUserName方法，下面五行代码就是该方法的实现
            Map<String, String> hashMap = new HashMap<>();
            hashMap.put("1", "张三");
            hashMap.put("2", "李四");
            hashMap.put("3", "王五");
            //真正想要代理的是queryUserName
            return "你被代理了 " + method.getName() + "：" + hashMap.get(args[0].toString());
        };
        //返回一个动态代理的对象
        //类型是IUserDao的bean对象
        return (IUserDao) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{IUserDao.class}, handler);
    }

    @Override
    public Class<?> getObjectType() {
        return IUserDao.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
