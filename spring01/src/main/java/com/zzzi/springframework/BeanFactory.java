package com.zzzi.springframework;

import java.util.HashMap;
import java.util.Map;

/**@author zzzi
 * @date 2023/10/29 20:54
 * 可以将其理解为一个IOC容器
 * 官方名称为Bean工厂
 */
public class BeanFactory {
    private Map<String,BeanDefinition> beanDefinitionMap=new HashMap<>();

    /**@author zzzi
     * @date 2023/10/29 20:55
     * 对外提供一个接口，可以通过bean的名称获取到对应的bean
     * 获取的bean也实现从IOC容器中获取到BeanDefinition这个对象
     * 然后调用这个对象中的getBean方法拿到真正的bean
     */
    public Object getBean(String name){
        return beanDefinitionMap.get(name).getBean();
    }
    /**@author zzzi
     * @date 2023/10/29 20:56
     * 对外提供接口，允许别人注入一个bean
     * 注入进来的bean存放在这个IOC容器中
     */
    public void registerBeanDefinition(String name,BeanDefinition beanDefinition){
        beanDefinitionMap.put(name,beanDefinition);
    }
}
