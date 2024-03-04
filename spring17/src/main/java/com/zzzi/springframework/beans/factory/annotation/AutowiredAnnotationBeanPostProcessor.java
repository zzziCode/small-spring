package com.zzzi.springframework.beans.factory.annotation;

import cn.hutool.core.bean.BeanUtil;
import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.PropertyValues;
import com.zzzi.springframework.beans.factory.BeanFactory;
import com.zzzi.springframework.beans.factory.BeanFactoryAware;
import com.zzzi.springframework.beans.factory.ConfigurableListableBeanFactory;
import com.zzzi.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import com.zzzi.springframework.util.ClassUtils;

import java.lang.reflect.Field;

/**
 * @author zzzi
 * @date 2023/11/13 15:41
 * 在这里处理注解属性填充个的功能
 */
public class AutowiredAnnotationBeanPostProcessor implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {
    private ConfigurableListableBeanFactory beanFactory;

    /**@author zzzi
     * @date 2023/12/17 21:14
     * 依次判断属性上是否使用了@Value或者@Autowired注解
     * 如果使用了，就进行相应的属性注入
     */
    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        //得到当前bean的真实类型（因为cglib继承了一次）
        Class<?> beanClass = bean.getClass();
        beanClass = ClassUtils.isCglibProxyClass(beanClass) ? beanClass.getSuperclass() : beanClass;

        //由于得到了真实的类型，这个类并没有继承，所以使用Declared也可以
        Field[] declaredFields = beanClass.getDeclaredFields();
        //1. 处理Value注解进行普通属性填充
        //所有的属性都要判断是否使用了Value注解
        for (Field field : declaredFields) {
            Value valueAnnotation = field.getAnnotation(Value.class);
            //不为空说明有Value注解注入普通属性
            if (valueAnnotation != null) {
                //拿到Value注解中占位符形式的字符串
                String value = valueAnnotation.value();
                //根据这个占位符中的名称从配置文件中拿到真正的值
                value = beanFactory.resolveEmbeddedValue(value);
                //得到占位符替换之后的真实值之后，直接属性填充
                BeanUtil.setFieldValue(bean, field.getName(), value);
            }
        }

        //2. 处理Autowired注解进行bean属性填充
        for (Field field : declaredFields) {
            Autowired autowiredAnnotation = field.getAnnotation(Autowired.class);
            if (autowiredAnnotation != null) {
                //拿到这个bean属性的类型
                Class<?> beanType = field.getType();
                String dependentBeanName = null;
                Object dependentBean = null;
                Qualifier qualifierAnnotation = field.getAnnotation(Qualifier.class);
                if (qualifierAnnotation != null) {//指定了所依赖的bean的名称
                    dependentBeanName = qualifierAnnotation.value();
                    dependentBean = beanFactory.getBean(dependentBeanName, beanType);
                } else {//没指定，只根据类型获取bean对象
                    dependentBean = beanFactory.getBean(beanType);
                }
                //直接进行属性填充
                BeanUtil.setFieldValue(bean, field.getName(), dependentBean);
            }
        }
        return pvs;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    /**
     * @author zzzi
     * @date 2023/11/13 15:42
     * 下面不在这里创建代理对象，所以返回null
     * 不在这里进行初始化前后的修改，所以返回原始bean
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        return true;
    }
}
