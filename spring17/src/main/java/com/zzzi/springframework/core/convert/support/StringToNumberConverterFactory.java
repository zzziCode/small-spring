package com.zzzi.springframework.core.convert.support;

import com.zzzi.springframework.core.convert.converter.Converter;
import com.zzzi.springframework.core.convert.converter.ConverterFactory;
import com.zzzi.springframework.util.NumberUtils;

/**@author zzzi
 * @date 2023/11/20 10:29
 * String转换为Number的转换器，内部调用NumberUtils中的方法
 */
public class StringToNumberConverterFactory implements ConverterFactory<String, Number> {
    @Override
    public <T extends Number> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToNumber<>(targetType);
    }

    private static final class StringToNumber<T extends Number> implements Converter<String, T> {
        private final Class<T> targetType;

        private StringToNumber(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T convert(String source) {
            if (source.isEmpty())
                return null;
            return NumberUtils.parseNumber(source, this.targetType);
        }
    }
}
