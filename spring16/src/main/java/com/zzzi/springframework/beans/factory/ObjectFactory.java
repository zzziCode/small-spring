package com.zzzi.springframework.beans.factory;

import com.zzzi.springframework.beans.BeansException;

/**
 * @author zzzi
 * @date 2023/11/16 19:28
 * 在这里定义一个函数式接口，后面要从三级缓存中拿到bean对象打破循环依赖时
 * 需要调用这个接口中提供的getObject方法
 */
public interface ObjectFactory<T> {
    T getObject() throws BeansException;
}
