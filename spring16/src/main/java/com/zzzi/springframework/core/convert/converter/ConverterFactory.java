package com.zzzi.springframework.core.convert.converter;

/**
 * @author zzzi
 * @date 2023/11/20 9:17
 * 类型转换器工厂，内部提供一个getConverter方法得到真正的转换器
 */
public interface ConverterFactory<S, R> {
    <T extends R> Converter<S, T> getConverter(Class<T> targetType);
}
