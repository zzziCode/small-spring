package com.zzzi.springframework.common;


import com.zzzi.springframework.bean.UserService;
import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.config.BeanPostProcessor;

public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if ("userService".equals(beanName)) {
            UserService userService = (UserService) bean;
            userService.setLocation("改为：北京");
        }
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
