package com.zzzi.springframework.beans.factory;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.PropertyValue;
import com.zzzi.springframework.beans.PropertyValues;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.BeanFactoryPostProcessor;
import com.zzzi.springframework.core.io.DefaultResourceLoader;
import com.zzzi.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Properties;

/**
 * @author zzzi
 * @date 2023/11/12 14:15
 * 在这里实现占位符替换的逻辑，在bean实例化之前自动触发，因为其实现了BeanFactoryPostProcessor
 * 之后这个类与正常的实例化前修改类一样要配置到xml文件中，之后就可以读取到其中的内容
 * 然后完成BeanDefinition的修改
 */
public class PropertyPlaceholderConfigurer implements BeanFactoryPostProcessor {
    public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
    public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";
    private String location;

    //按照编程习惯给属性加上set和get方法
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @author zzzi
     * @date 2023/11/12 14:16
     * 在这里根据属性文件的位置来根据占位符中的键读取到属性文件中的值
     * 从而完成占位符的替换
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //根据属性文件的路径加载其中的内容
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(location);
        Properties properties = new Properties();
        try {
            //加载属性文件中的内容
            properties.load(resource.getInputStream());

            //依次遍历所有的bean，将bean中的占位符替换
            String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
            for (String beanDefinitionName : beanDefinitionNames) {
                //一次拿到bean的注册信息，尝试去修改其中的PropertyValues中保存的属性中的value
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
                PropertyValues propertyValues = beanDefinition.getPropertyValues();
                //依次遍历当前bean的所有属性，从而尝试进行修改
                for (PropertyValue propertyValue : propertyValues.getPropertyValues()) {
                    Object value = propertyValue.getValue();
                    //属性值中只有是String的才有可能存在占位符，因为占位符的配置过程中以String出现
                    if (value instanceof String) {
                        //看当前的属性值中是否存在"${"和"}"
                        String strVal = (String) value;
                        /**@author zzzi
                         * @date 2023/11/12 14:32
                         * 核心就是下面几步，尝试改变每一个bean中的占位符属性（如果有的话）
                         */
                        int start = strVal.indexOf(DEFAULT_PLACEHOLDER_PREFIX);
                        int end = strVal.indexOf(DEFAULT_PLACEHOLDER_SUFFIX);
                        //当前的属性值中真的有"${"和"}"，开始替换
                        if (start != -1 && end != -1 && start < end) {
                            //拿到键，根据键去属性文件中找到值
                            String propKey = strVal.substring(start + 2, end);
                            String propValue = properties.getProperty(propKey);
                            StringBuffer buffer = new StringBuffer(strVal);

                            //占位符的替换
                            buffer.replace(start, end + 1, propValue);
                            //将新的属性添加到bean的注册信息中，而不是替换旧的
                            propertyValues.addPropertyValue(new PropertyValue(propertyValue.getName(), buffer.toString()));
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new BeansException("Could not load properties", e);
        }
    }
}
