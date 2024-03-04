package com.zzzi.springframework.common;


import com.zzzi.springframework.bean.UserDao;
import com.zzzi.springframework.bean.UserService;
import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.config.BeanPostProcessor;
/**@author zzzi
 * @date 2023/11/3 16:38
 * 在这里实现实例化之后的自定义修改操作
 */
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if ("userService".equals(beanName)) {
            UserService userService = (UserService) bean;
            userService.setLocation("实例化后改为：北京");
        }
        //如果增加这一段代码，那么userDao在创建过程中也能匹配上实例化后的修改逻辑
        /*if ("userDao".equals(beanName)) {
            UserDao.hashMap.put("4","新增一条");
        }*/
        return bean;
    }

    /**@author zzzi
     * @date 2023/11/2 19:51
     * 不做任何修改
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
