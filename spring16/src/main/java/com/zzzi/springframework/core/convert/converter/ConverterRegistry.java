package com.zzzi.springframework.core.convert.converter;

/**
 * @author zzzi
 * @date 2023/11/20 9:20
 * 实现这个接口的类外部可以调用这些待实现的方法从而将转换器进行注册
 * 一共有三种注册的接口
 */
public interface ConverterRegistry {
    void addConverter(Converter<?, ?> converter);

    void addConverterFactory(ConverterFactory<?, ?> converterFactory);

    void addConverter(GenericConverter converter);
}
