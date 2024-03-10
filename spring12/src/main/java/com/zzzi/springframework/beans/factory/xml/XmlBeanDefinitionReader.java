package com.zzzi.springframework.beans.factory.xml;

import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.PropertyValue;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.BeanReference;
import com.zzzi.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import com.zzzi.springframework.beans.factory.support.BeanDefinitionRegistry;
import com.zzzi.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import com.zzzi.springframework.core.io.Resource;
import com.zzzi.springframework.core.io.ResourceLoader;
import cn.hutool.core.util.StrUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * @author zzzi
 * @date 2023/11/4 14:59
 * 在这里实现加载配置文件
 * 将获取配置文件的输入流和从配置文件中读取所有的BeanDefinition都集成到这一个类中
 */
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {
    public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
        super(registry);
    }

    public XmlBeanDefinitionReader(BeanDefinitionRegistry registry, ResourceLoader resourceLoader) {
        super(registry, resourceLoader);
    }

    @Override
    public void loadBeanDefinitions(Resource resource) throws BeansException {
        try (
                InputStream is = resource.getInputStream();
        ) {
            doLoadBeanDefinitions(is);
        } catch (IOException | ClassNotFoundException | DocumentException e) {
            throw new BeansException("IOException parsing XML document from " + resource, e);
        }
    }

    /**
     * @author zzzi
     * @date 2023/11/4 14:53
     * 将四种loadBeanDefinitions方法的执行整合到这一个方法中执行
     */
    protected void doLoadBeanDefinitions(InputStream inputStream) throws ClassNotFoundException, DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();

        /**@author zzzi
         * @date 2023/11/12 11:00
         * 第一种读取bean的方法：更加快捷，不用一个一个手写配置文件，只需要使用Component注解即可
         * 读取xml配置包路径，将当前路径下类上使用Component的bean加入到注册表
         * 不管是<bean></bean>还是<component-scan></component-scan>，都是配置文件中的根标签
         */
        // 解析 context:component-scan 标签，扫描包中的类并提取相关信息，用于组装 BeanDefinition
        Element componentScan = root.element("component-scan");
        if (null != componentScan) {
            //读取到xml配置文件中设置的包扫描路径，进行扫描
            String scanPath = componentScan.attributeValue("base-package");
            if (StrUtil.isEmpty(scanPath)) {
                throw new BeansException("The value of base-package attribute can not be empty or null");
            }
            //包扫描成功之后，内部所有的bean的属性都没有注入
            scanPackage(scanPath);
        }

        /**@author zzzi
         * @date 2023/11/12 11:00
         * 第二种读取bean的方法：更加原始
         * 读取xml配置文件中直接配置的bean加入到注册表
         * 有了包扫描路径这种也要接着扫描，防止既配置了扫描路径，又单独配置了xml形式的bean
         */
        List<Element> beanList = root.elements("bean");
        for (Element bean : beanList) {

            String id = bean.attributeValue("id");
            String name = bean.attributeValue("name");
            String className = bean.attributeValue("class");
            String initMethod = bean.attributeValue("init-method");
            String destroyMethodName = bean.attributeValue("destroy-method");
            String beanScope = bean.attributeValue("scope");

            // 获取 Class，方便获取类中的名称
            Class<?> clazz = Class.forName(className);
            // 优先级 id > name
            String beanName = StrUtil.isNotEmpty(id) ? id : name;
            if (StrUtil.isEmpty(beanName)) {
                beanName = StrUtil.lowerFirst(clazz.getSimpleName());
            }

            // 定义Bean
            BeanDefinition beanDefinition = new BeanDefinition(clazz);
            beanDefinition.setInitMethodName(initMethod);
            beanDefinition.setDestroyMethodName(destroyMethodName);

            if (StrUtil.isNotEmpty(beanScope)) {
                beanDefinition.setScope(beanScope);
            }

            List<Element> propertyList = bean.elements("property");
            // 读取属性并填充
            for (Element property : propertyList) {
                // 解析标签：property
                String attrName = property.attributeValue("name");
                String attrValue = property.attributeValue("value");
                String attrRef = property.attributeValue("ref");
                // 获取属性值：引入对象、值对象
                //如果有bean的依赖，那么就
                Object value = StrUtil.isNotEmpty(attrRef) ? new BeanReference(attrRef) : attrValue;
                // 创建属性信息
                PropertyValue propertyValue = new PropertyValue(attrName, value);
                beanDefinition.getPropertyValues().addPropertyValue(propertyValue);
            }
            if (getRegistry().containsBeanDefinition(beanName)) {
                throw new BeansException("Duplicate beanName[" + beanName + "] is not allowed");
            }
            // 注册 BeanDefinition
            getRegistry().registerBeanDefinition(beanName, beanDefinition);
        }
    }

    /**@author zzzi
     * @date 2023/11/12 15:09
     * 在xml文件读取中加入这个方法就引入了自动注册的模块
     */
    private void scanPackage(String scanPath) {
        //由于读取到的包扫描路径可能是多个，所以首先解析出包扫描的路径
        String[] split = scanPath.split(",");
        /**@author zzzi
         * @date 2024/3/10 14:28
         * 这里传入的registry可以理解为是一个beanFactory
         * 所以通过包扫描路径得到的BeanDefinition就可以放入beanFactory中的beanDefinitionMap中
         */
        ClassPathBeanDefinitionScanner classPathBeanDefinitionScanner = new ClassPathBeanDefinitionScanner(getRegistry());
        //调用其中的doScan方法就可以自动注册
        classPathBeanDefinitionScanner.doScan(split);
    }

    @Override
    public void loadBeanDefinitions(Resource... resources) throws BeansException {
        for (Resource resource : resources) {
            loadBeanDefinitions(resource);
        }
    }

    @Override
    public void loadBeanDefinitions(String location) throws BeansException {
        ResourceLoader resourceLoader = getResourceLoader();
        Resource resource = resourceLoader.getResource(location);
        loadBeanDefinitions(resource);
    }

    @Override
    public void loadBeanDefinitions(String... locations) throws BeansException {
        for (String location : locations) {
            loadBeanDefinitions(location);
        }

    }

}
