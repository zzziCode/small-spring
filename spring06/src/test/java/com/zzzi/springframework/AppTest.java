package com.zzzi.springframework;


import com.zzzi.springframework.bean.UserService;
import com.zzzi.springframework.beans.factory.support.DefaultListableBeanFactory;
import com.zzzi.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import com.zzzi.springframework.common.MyBeanFactoryPostProcessor;
import com.zzzi.springframework.common.MyBeanPostProcessor;
import com.zzzi.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.Test;

/**
 * @author zzzi
 * @date 2023/11/3 13:23
 * 在这里执行测试
 */
public class AppTest {
    /**
     * @author zzzi
     * @date 2023/11/3 15:12
     * 调用原始的DefaultListableBeanFactory核心类，然后手动控制修改逻辑的执行
     * 这种方式需要手动控制，因为spring.xml文件中并没有将修改策略定义成bean
     */
    @Test
    public void testBeanFactoryPostProcessorAndBeanPostProcessor() {
        //1. 获取beanFactory
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        //2. 读取配置文件
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions("classpath:spring.xml");

        //3. 创建一个实例化前的修改策略并直接执行
        MyBeanFactoryPostProcessor factoryPostProcessor = new MyBeanFactoryPostProcessor();
        factoryPostProcessor.postProcessBeanFactory(beanFactory);

        //4. 创建一个实例化后的修改策略并加入容器中
        MyBeanPostProcessor postProcessor = new MyBeanPostProcessor();
        beanFactory.addBeanPostProcessor(postProcessor);

        //5. 获取bean对象，此时经历三步：
        //1. 创建空bean
        //2. 属性填充
        //3. 执行实例化后的修改策略
        UserService userService = beanFactory.getBean("userService", UserService.class);
        System.out.println(userService);
    }

    /**
     * @author zzzi
     * @date 2023/11/3 15:12
     * 测试包装好了的应用上下文，内部在bean的生命周期中自动调用修改的逻辑
     * 这种方法在内部可以直接按照类型获取到自定义的修改策略
     * 然后底层自动执行
     */
    @Test
    public void testApplicationContext() {
        //1. 获取应用上下文
        ClassPathXmlApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("classpath:springPostProcessor.xml");
        //2. 直接获取修改后的bean对象
        UserService userService = applicationContext.getBean("userService", UserService.class);
        System.out.println(userService);
    }
}
