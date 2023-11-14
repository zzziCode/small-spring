package com.zzzi.springframework.context.support;

import com.zzzi.springframework.beans.factory.support.DefaultListableBeanFactory;
import com.zzzi.springframework.beans.factory.xml.XmlBeanDefinitionReader;

public abstract class AbstractXmlApplicationContext extends AbstractRefreshableApplicationContext {
    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) {
        //得到读取文件的组件
        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory, this);
        String[] configLocations = getConfigLocations();
        if (configLocations != null)
            //根据传递来的文件配置来读取文件中的信息，之后将其保存到BeanDefinition中
            xmlBeanDefinitionReader.loadBeanDefinitions(configLocations);
    }

    protected abstract String[] getConfigLocations();

}
