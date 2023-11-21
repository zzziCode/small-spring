package com.zzzi.springframework.core.convert.support;

import com.zzzi.springframework.core.convert.converter.ConverterRegistry;

/**@author zzzi
 * @date 2023/11/20 10:21
 * 外部调用这个类，既可以使用GenericConversionService类中的方法
 * 又可以得到一个默认的类型转换器
 */
public class DefaultConversionService extends GenericConversionService {
    public DefaultConversionService() {
        addDefaultConverters(this);
    }

    public static void addDefaultConverters(ConverterRegistry converterRegistry) {
        converterRegistry.addConverterFactory(new StringToNumberConverterFactory());
    }
}
