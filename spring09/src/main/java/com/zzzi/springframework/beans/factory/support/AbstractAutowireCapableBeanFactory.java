package com.zzzi.springframework.beans.factory.support;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.PropertyValue;
import com.zzzi.springframework.beans.PropertyValues;
import com.zzzi.springframework.beans.factory.config.AutowireCapableBeanFactory;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.config.BeanPostProcessor;
import com.zzzi.springframework.beans.factory.config.BeanReference;
import com.zzzi.springframework.beans.factory.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {
    public InstantiationStrategy getInstantiationStrategy() {
        return instantiationStrategy;
    }

    public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
        this.instantiationStrategy = instantiationStrategy;
    }

    //默认的实例化策略
    private InstantiationStrategy instantiationStrategy = new CglibSubclassingInstantiationStrategy();

    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
        Object bean = null;
        try {
            bean = createBeanInstance(beanDefinition, beanName, args);
            // 给 Bean 填充属性
            applyPropertyValues(beanName, bean, beanDefinition);
            // 执行 Bean 的初始化方法和 BeanPostProcessor 的前置和后置处理方法
            /**@author zzzi
             * @date 2023/11/3 19:38
             * 在这里面调用初始化的方法，需要执行
             */
            bean = initializeBean(beanName, bean, beanDefinition);
        } catch (Exception e) {
            throw new BeansException("Instantiation of bean failed", e);
        }

        /**@author zzzi
         * @date 2023/11/4 14:38
         * 在这里保存了所有需要执行的逻辑
         */
        // 注册实现了 DisposableBean 接口的 Bean 对象
        registerDisposableBeanIfNecessary(beanName, bean, beanDefinition);
        /**@author zzzi
         * @date 2023/11/7 9:53
         * 新增的判断逻辑
         */
        if(beanDefinition.isSingleton())
            addSingleton(beanName, bean);
        return bean;
    }

    private void registerDisposableBeanIfNecessary(String beanName, Object bean, BeanDefinition beanDefinition) {
        /**@author zzzi
         * @date 2023/11/7 9:53
         * 新增的判断逻辑，非单例模式不保存销毁逻辑
         */
        if(!beanDefinition.isSingleton())
            return;
        if (bean instanceof DisposableBean || StrUtil.isNotEmpty(beanDefinition.getDestroyMethodName())) {
            /**@author zzzi
             * @date 2023/11/3 19:50
             * 将实现接口从而编写注册和销毁的方式，以及配置文件中指定注册和销毁的方式
             * 这两种整合到一起
             */
            registerDisposableBean(beanName, new DisposableBeanAdapter(bean, beanName, beanDefinition));
        }
    }


    private Object createBeanInstance(BeanDefinition beanDefinition, String beanName, Object[] args) {
        if (args == null)
            return instantiationStrategy.instantiate(beanDefinition, beanName, null, null);
        Constructor constructorToUse = null;
        Class beanClass = beanDefinition.getBeanClass();
        Constructor[] declaredConstructors = beanClass.getDeclaredConstructors();
        for (Constructor constructor : declaredConstructors) {
            Class[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length != args.length)
                continue;
            int i = 0;
            for (; i < parameterTypes.length; ++i) {
                if (parameterTypes[i] != args[i].getClass())
                    break;
            }
            if (i == parameterTypes.length) {
                constructorToUse = constructor;
                break;
            }
        }
        return instantiationStrategy.instantiate(beanDefinition, beanName, constructorToUse, args);
    }


    private void applyPropertyValues(String beanName, Object bean, BeanDefinition beanDefinition) {
        try {
            PropertyValues declaredPropertyValues = beanDefinition.getPropertyValues();
            PropertyValue[] propertyValues = declaredPropertyValues.getPropertyValues();
            for (PropertyValue propertyValue : propertyValues) {
                String name = propertyValue.getName();
                Object value = propertyValue.getValue();
                if (value instanceof BeanReference) {
                    BeanReference beanReference = (BeanReference) value;
                    /**@author zzzi
                     * @date 2024/3/7 14:58
                     * 这里的getBean还是会有两种情况：
                     * 1. xml文件则直接返回
                     * 2. factoryBean方式需要获取内部真正的bean返回
                     */
                    value = getBean(beanReference.getBeanName());
                }
                BeanUtil.setFieldValue(bean, name, value);
            }
        } catch (BeansException e) {
            throw new BeansException("Error setting property values：" + beanName);
        }
    }


    private Object initializeBean(String beanName, Object bean, BeanDefinition beanDefinition) {
        /**@author zzzi
         * @date 2023/11/6 15:52
         * 在这里增加一个容器资源注入的操作
         */
        // 1. 容器资源直接注入
        if(bean instanceof BeanFactoryAware){
            ((BeanFactoryAware) bean).setBeanFactory(this);
        }
        if(bean instanceof BeanClassLoaderAware){
            ((BeanClassLoaderAware) bean).setBeanClassLoader(getClassLoader());
        }
        if(bean instanceof BeanNameAware){
            ((BeanNameAware) bean).setBeanName(beanName);
        }
        /**@author zzzi
         * @date 2023/11/6 15:55
         * 在执行BeanPostProcessor Before处理的时候，会触发修改逻辑，包装处理器的逻辑也会触发
         * 此时在包装处理器中完成容器资源注入
         */
        // 2. 执行 BeanPostProcessor Before 处理
        Object wrappedBean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);

        // 执行 Bean 对象的初始化方法
        try {
            invokeInitMethods(beanName, wrappedBean, beanDefinition);
        } catch (Exception e) {
            throw new BeansException("Invocation of init method of bean[" + beanName + "] failed", e);
        }

        // 3. 执行 BeanPostProcessor After 处理
        wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
        return wrappedBean;
    }

    private void invokeInitMethods(String beanName, Object wrappedBean, BeanDefinition beanDefinition) throws Exception {
        //1. 实现接口从而实现初始化方法
        if (wrappedBean instanceof InitializingBean)
            ((InitializingBean) wrappedBean).afterPropertiesSet();

        //2. 注解配置实现初始化方法
        String initMethodName = beanDefinition.getInitMethodName();
        if (StrUtil.isNotEmpty(initMethodName) && !(wrappedBean instanceof InitializingBean)) {
            Method initMethod = beanDefinition.getBeanClass().getMethod(initMethodName);
            if (null == initMethod) {
                throw new BeansException("Could not find an init method named '" + initMethodName + "' on bean with name '" + beanName + "'");
            }
            //利用反射执行配置文件中配置的初始化方法
            initMethod.invoke(wrappedBean);
        }
    }


    @Override
    public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) throws BeansException {
        Object result = existingBean;
        List<BeanPostProcessor> beanPostProcessors = getBeanPostProcessors();
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            Object current = beanPostProcessor.postProcessBeforeInitialization(result, beanName);
            if (current != null)
                result = current;
        }
        return result;
    }

    @Override
    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) throws BeansException {
        Object result = existingBean;
        List<BeanPostProcessor> beanPostProcessors = getBeanPostProcessors();
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            Object current = beanPostProcessor.postProcessAfterInitialization(result, beanName);
            if (current != null)
                result = current;
        }
        return result;
    }
}
