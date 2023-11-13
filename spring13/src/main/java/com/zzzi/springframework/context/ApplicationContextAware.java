package com.zzzi.springframework.context;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.factory.Aware;

/**
 * @author zzzi
 * @date 2023/11/6 15:44
 * 实现这个接口的bean，spring认为其需要ApplicationContext这个容器资源
 * 于是通过set接口注入
 */
public interface ApplicationContextAware extends Aware {
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException;
}
