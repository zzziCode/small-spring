package com.zzzi.springframework.context.support;

import com.zzzi.springframework.beans.factory.support.DefaultListableBeanFactory;
import com.zzzi.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**@author zzzi
 * @date 2023/11/3 13:06
 * 在这里读取xml配置文件，调用的是之前扩展的加载配置文件的接口
 * 将资源加载器的获取，以及配置文件的解析都封装到了这一个方法中
 */
public abstract class AbstractXmlApplicationContext extends AbstractRefreshableApplicationContext {
    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) {
        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory, this);
        //不管如何，可能提供了多个配置文件
        String[] configLocations = getConfigLocations();
        if(configLocations!=null){
            xmlBeanDefinitionReader.loadBeanDefinitions(configLocations);
        }
    }

    protected abstract String[] getConfigLocations();
}