package com.zzzi.springframework.core.convert;

import com.sun.istack.internal.Nullable;

/**
 * @author zzzi
 * @date 2023/11/20 9:13
 * 在这里定义两个待实现的方法，供属性填充时直接调用
 */
public interface ConversionService {
    boolean canConvert(@Nullable Class<?> sourceType, Class<?> targetType);

    <T> T convert(Object source, Class<T> targetType);
}
