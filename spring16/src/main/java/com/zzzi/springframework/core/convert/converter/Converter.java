package com.zzzi.springframework.core.convert.converter;

/**
 * @author zzzi
 * @date 2023/11/20 9:15
 * 所有的类型转换器都要实现这个接口，标记自身是一个类型转换器
 * 实现内部的convert方法，内部定义类型转换的逻辑
 */
public interface Converter<S, T> {
    T convert(S source);
}
