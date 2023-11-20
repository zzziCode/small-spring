package com.zzzi.springframework.beans.factory.support;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.zzzi.springframework.beans.BeansException;
import com.zzzi.springframework.beans.PropertyValue;
import com.zzzi.springframework.beans.PropertyValues;
import com.zzzi.springframework.beans.factory.*;
import com.zzzi.springframework.beans.factory.config.*;
import com.zzzi.springframework.core.convert.ConversionService;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {


    //默认的实例化策略
    /**
     * @author zzzi
     * @date 2023/11/14 14:02
     * 在这里修改默认的实例化策略为JDK
     */
    private InstantiationStrategy instantiationStrategy = new SimpleInstantiationStrategy();

    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
        Object bean = resolveBeforeInstantiation(beanName, beanDefinition);
        if (null != bean) {//为空代表不需要代理，正常执行普通bean的创建
            return bean;
        }
        return doCreateBean(beanName, beanDefinition, args);
    }

    /**
     * @author zzzi
     * @date 2023/11/16 20:03
     * 抽象出一个新的方法，bean的创建过程移动到了这里
     */
    protected Object doCreateBean(String beanName, BeanDefinition beanDefinition, Object[] args) {
        Object bean = null;
        try {
            /**@author zzzi
             * @date 2023/11/11 16:57
             * 为了引入AOP机制，在创建普通bean之前引入新的 一步
             */

            bean = createBeanInstance(beanDefinition, beanName, args);
            /**@author zzzi
             * @date 2023/11/16 20:03
             * 在这里增加一步，当空bean创建完成之后，将其保存到第三级缓存中
             */
            if (beanDefinition.isSingleton()) {
                Object finalBean = bean;
                addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, beanDefinition, finalBean));
            }
            // 实例化后判断
            boolean continueWithPropertyPopulation = applyBeanPostProcessorsAfterInstantiation(beanName, bean);
            if (!continueWithPropertyPopulation) {
                return bean;
            }
            /**@author zzzi
             * @date 2023/11/13 16:22
             * 在这里新增一步，触发注解填充属性的功能
             */
            applyBeanPostProcessorsBeforeApplyingPropertyValues(beanName, bean, beanDefinition);

            // 给 Bean 填充属性
            applyPropertyValues(beanName, bean, beanDefinition);
            // 执行 Bean 的初始化方法和 BeanPostProcessor 的前置和后置处理方法
            /**@author zzzi
             * @date 2023/11/3 19:38
             * 在这里面调用初始化的方法，需要执行
             * 如果执行了AOP，那么此处的返回值不再是一个普通的bean，而是bean的代理对象（被增强的bean）
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
        Object exposedBean = bean;
        if (beanDefinition.isSingleton()) {
            /**@author zzzi
             * @date 2023/11/16 20:08
             * 这里为了保持单例性，直接尝试从三级缓存中拿到已经创建好的bean对象
             */
            exposedBean = getSingleton(beanName);
            registerSingleton(beanName, exposedBean);
        }
        return bean;
    }

    private Object getEarlyBeanReference(String beanName, BeanDefinition beanDefinition, Object finalBean) {
        Object exposedBean = finalBean;
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                exposedBean = ((InstantiationAwareBeanPostProcessor) beanPostProcessor).getEarlyBeanReference(exposedBean, beanName);
                if (exposedBean == null)
                    return exposedBean;
            }
        }
        return exposedBean;
    }

    /**
     * @author zzzi
     * @date 2023/11/14 13:55
     * 新增的方法，依次触发postProcessAfterInstantiation方法的执行
     */
    private boolean applyBeanPostProcessorsAfterInstantiation(String beanName, Object bean) {
        boolean continueWithPropertyPopulation = true;
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                InstantiationAwareBeanPostProcessor instantiationAwareBeanPostProcessor = (InstantiationAwareBeanPostProcessor) beanPostProcessor;
                if (!instantiationAwareBeanPostProcessor.postProcessAfterInstantiation(bean, beanName)) {
                    continueWithPropertyPopulation = false;
                    break;
                }
            }
        }
        return continueWithPropertyPopulation;
    }

    /**
     * @author zzzi
     * @date 2023/11/13 16:25
     * 这是新增的一个方法，引入了注解属性填充的模块
     */
    private void applyBeanPostProcessorsBeforeApplyingPropertyValues(String beanName, Object bean, BeanDefinition beanDefinition) {
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            //实现了这个接口就调用其中的注解属性填充方法
            if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                PropertyValues pvs = ((InstantiationAwareBeanPostProcessor) beanPostProcessor).
                        postProcessPropertyValues(beanDefinition.getPropertyValues(), bean, beanName);
                if (pvs != null) {//只要没在xml文件中配置，这里就是长度为零的容器
                    for (PropertyValue propertyValue : pvs.getPropertyValues()) {
                        beanDefinition.getPropertyValues().addPropertyValue(propertyValue);
                    }
                }
            }
        }
    }

    /**
     * @author zzzi
     * @date 2023/11/11 17:01
     * 新增的方法，为了引入AOP机制
     */
    private Object resolveBeforeInstantiation(String beanName, BeanDefinition beanDefinition) {
        //尝试执行前置修改，如果xml配置文件中有这种类型的bean存在的话，就会返回代理对象
        Object bean = applyBeanPostProcessorsBeforeInstantiation(beanDefinition.getBeanClass(), beanName);
        if (null != bean) {
            //如果得到代理对象，那么就执行后置修改，然后返回
            bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
        }
        //返回的bean可能为空
        return bean;
    }

    /**
     * @author zzzi
     * @date 2023/11/11 17:01
     * 新增的方法，为了执行xml配置文件中的AOP核心bean中的配置
     * 在普通bean的实例化之前执行
     */
    private Object applyBeanPostProcessorsBeforeInstantiation(Class beanClass, String beanName) {
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            //当前的实例化前策略是一个切面策略
            if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                //得到代理对象返回，可能由于切入点表达式无法匹配从而造成代理对象为空
                Object result = ((InstantiationAwareBeanPostProcessor) beanPostProcessor).postProcessBeforeInstantiation(beanClass, beanName);
                if (null != result) return result;
            }
        }
        return null;
    }

    private void registerDisposableBeanIfNecessary(String beanName, Object bean, BeanDefinition beanDefinition) {
        /**@author zzzi
         * @date 2023/11/7 9:53
         * 新增的判断逻辑，非单例模式不保存销毁逻辑
         */
        if (beanDefinition.isSingleton())
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
                //bean的属性是另外一个bean，那么就需要进行依赖注入
                if (value instanceof BeanReference) {
                    BeanReference beanReference = (BeanReference) value;
                    value = getBean(beanReference.getBeanName());
                /**@author zzzi
                 * @date 2023/11/20 10:55
                 * 属性填充更改
                 */
                } else {//普通属性需要先类型转换再进行属性填充
                    //得到两个属性
                    Class<?> sourceType = value.getClass();
                    Class<?> targetType = (Class<?>) TypeUtil.getFieldType(bean.getClass(), name);
                    //获取类型转换服务
                    ConversionService conversionService = getConversionService();
                    if (conversionService.canConvert(sourceType, targetType)) {
                        value = conversionService.convert(value, targetType);
                    }
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
         * 在这里回调实现的接口中的方法
         */
        // 1. 容器资源直接注入
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(this);
        }
        if (bean instanceof BeanClassLoaderAware) {
            ((BeanClassLoaderAware) bean).setBeanClassLoader(getClassLoader());
        }
        if (bean instanceof BeanNameAware) {
            ((BeanNameAware) bean).setBeanName(beanName);
        }
        /**@author zzzi
         * @date 2023/11/6 15:55
         * 在执行BeanPostProcessor Before处理的时候，会触发修改逻辑，包装处理器的逻辑也会触发
         * 此时在包装处理器中完成容器资源注入
         */
        // 2. 执行 BeanPostProcessor Before 处理，初始化之前的操作
        Object wrappedBean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);

        // 执行 Bean 对象的初始化方法
        try {
            invokeInitMethods(beanName, wrappedBean, beanDefinition);
        } catch (Exception e) {
            throw new BeansException("Invocation of init method of bean[" + beanName + "] failed", e);
        }

        // 3. 执行 BeanPostProcessor After 处理，初始化之后的操作（AOP？？）
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

    public InstantiationStrategy getInstantiationStrategy() {
        return instantiationStrategy;
    }

    public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
        this.instantiationStrategy = instantiationStrategy;
    }
}
