package com.zzzi.springframework.converter;

import com.zzzi.springframework.beans.factory.FactoryBean;

import java.util.HashSet;
import java.util.Set;

public class ConverterFactoryBean implements FactoryBean<Set<?>> {

    @Override
    public Set<?> getObject() throws Exception {
        HashSet<Object> converters=new HashSet<>();
        StringToLocalDateConverter localDateConverter = new StringToLocalDateConverter("yyyy-MM-dd");
        converters.add(localDateConverter);
        return converters;
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
