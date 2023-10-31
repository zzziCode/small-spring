package com.zzzi.springframework.beans.factory.support;

import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.BeansException;
import com.zzzi.springframework.beans.factory.config.InstantiationStrategy;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.Constructor;
/**@author zzzi
 * @date 2023/10/31 19:40
 * 第二种实例化bean对象的策略：使用CGlib的继承，并使用字节码来实现
 * 注意这里需要引入cglib的依赖
 */
public class CglibSubclassingInstantistionStrategy implements InstantiationStrategy {
    @Override
    public Object instantiate(BeanDefinition beanDefinition, String beanName, Constructor ctor, Object[] args) throws BeansException {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(beanDefinition.getBeanClass());
        enhancer.setCallback(new NoOp() {
            @Override
            public int hashCode() {
                return super.hashCode();
            }
        });
        if (null == ctor) return enhancer.create();
        return enhancer.create(ctor.getParameterTypes(), args);
    }
}
