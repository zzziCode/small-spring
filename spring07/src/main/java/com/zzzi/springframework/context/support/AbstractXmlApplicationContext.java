package com.zzzi.springframework.context.support;

import com.zzzi.springframework.beans.factory.support.DefaultListableBeanFactory;
import com.zzzi.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import com.zzzi.springframework.core.io.ResourceLoader;

public abstract class AbstractXmlApplicationContext extends AbstractRefreshableApplicationContext {
    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) {
        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory, this);
        String[] configLocations = getConfigLocations();
        if (configLocations != null)
            xmlBeanDefinitionReader.loadBeanDefinitions(configLocations);
    }

    protected abstract String[] getConfigLocations();

}
