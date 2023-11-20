package com.zzzi.springframework.beans.factory.config;

import com.zzzi.springframework.beans.factory.HierarchicalBeanFactory;
import com.zzzi.springframework.core.convert.ConversionService;
import com.zzzi.springframework.util.StringValueResolver;

/**
 * @author zzzi
 * @date 2023/11/4 13:29
 * 在这里保存spring中的配置信息，并定义一个待实现的保存实例化后修改策略的方法
 */
public interface ConfigurableBeanFactory extends SingletonBeanRegistry, HierarchicalBeanFactory {
    String SCOPE_SINGLETON = "singleton";

    String SCOPE_PROTOTYPE = "prototype";

    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

    void destroySingletons();

    /**
     * @author zzzi
     * @date 2023/11/13 15:48
     * 新增两个方法，用来保存字符串处理器
     */
    void addEmbeddedValueResolver(StringValueResolver valueResolver);

    String resolveEmbeddedValue(String value);

    /**@author zzzi
     * @date 2023/11/20 10:47
     * 新增两个方法用来操作类型转换服务
     */
    void setConversionService(ConversionService conversionService);

    ConversionService getConversionService();
}
