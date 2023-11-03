package com.zzzi.springframework.context.support;

import com.zzzi.springframework.beans.factory.support.DefaultListableBeanFactory;
import com.zzzi.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**@author zzzi
 * @date 2023/11/3 13:06
 * 在这里读取xml配置文件
 */
public abstract class AbstractXmlApplicationContext extends AbstractRefreshableApplicationContext {
    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) {
        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory, this);
        String[] configLocations = getConfigLocations();
        if(configLocations!=null){
            xmlBeanDefinitionReader.loadBeanDefinitions(configLocations);
        }
    }

    protected abstract String[] getConfigLocations();
}