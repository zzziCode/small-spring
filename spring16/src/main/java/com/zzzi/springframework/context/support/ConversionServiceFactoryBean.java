package com.zzzi.springframework.context.support;

import com.sun.istack.internal.Nullable;
import com.zzzi.springframework.beans.factory.FactoryBean;
import com.zzzi.springframework.beans.factory.InitializingBean;
import com.zzzi.springframework.core.convert.converter.Converter;
import com.zzzi.springframework.core.convert.converter.ConverterFactory;
import com.zzzi.springframework.core.convert.converter.GenericConverter;
import com.zzzi.springframework.core.convert.support.DefaultConversionService;
import com.zzzi.springframework.core.convert.support.GenericConversionService;

import java.util.Set;

/**
 * @author zzzi
 * @date 2023/11/20 10:33
 * xml文件以这个为入口，得到项目中配置的类型转换服务
 */
public class ConversionServiceFactoryBean implements FactoryBean, InitializingBean {
    @Nullable
    private Set<?> converters;
    @Nullable
    private GenericConversionService conversionService;

    @Override
    public Object getObject() throws Exception {
        return conversionService;
    }

    @Override
    public Class<?> getObjectType() {
        return conversionService.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**@author zzzi
     * @date 2023/11/20 10:34
     * 在初始化方法中创建一个类型转换服务的对象，将所有保存的类型转换器保存到类型转换服务中
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.conversionService=new DefaultConversionService();
        registerConverters(converters,conversionService);
    }

    /**@author zzzi
     * @date 2023/11/20 10:37
     * 将当前xml文件中读取到的converters全部注册到类型转换服务中的converters中
     */
    private void registerConverters(Set<?> converters, GenericConversionService conversionService) {
        if(converters!=null){
            for (Object converter : converters) {
                if(converters instanceof GenericConverter){
                    conversionService.addConverter((GenericConverter) converter);
                }else if(converter instanceof Converter){
                    conversionService.addConverter((Converter<?, ?>) converter);
                }else if(converter instanceof ConverterFactory){
                    conversionService.addConverterFactory((ConverterFactory<?, ?>) converter);
                }else{//当前的类型转换器不是上述三种类型中的任何一种就会报错
                    throw new IllegalArgumentException("Each converter object must implement one of the " +
                            "Converter, ConverterFactory, or GenericConverter interfaces");
                }
            }
        }
    }
}
