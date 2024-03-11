package com.zzzi.springframework.context.annotation;

import cn.hutool.core.util.StrUtil;
import com.zzzi.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.beans.factory.support.BeanDefinitionRegistry;
import com.zzzi.springframework.sterotype.Component;

import java.util.Set;

/**
 * @author zzzi
 * @date 2023/11/12 14:50
 * 自动注册的核心类，可以将包路径下的所有使用Component注解的类得到
 * 并且根据Scope注解的使用来设置bean的作用域
 * 最后将这些类和其对应的BeanDefinition保存到注册表中
 */
public class ClassPathBeanDefinitionScanner extends ClassPathScanningCandidateComponentProvider {
    //调用这个可以保存
    private BeanDefinitionRegistry registry;

    //对外提供接口，可以设置BeanDefinitionRegistry
    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    /**
     * @author zzzi
     * @date 2023/11/12 15:01
     * 根据传递来的包扫描路径从而扫描到所有的bean保存到注册表中
     */
    public void doScan(String... basePackages) {
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = findCandidateComponents(basePackage);
            //遍历拿到的所有BeanDefinition，尝试修改器作用域，然后将每一个BeanDefinition保存到注册表中
            for (BeanDefinition candidateComponent : candidateComponents) {
                String scope = resolveScope(candidateComponent);
                if (!scope.equals("")) {
                    candidateComponent.setScope(scope);
                }
                String beanName = determineBeanName(candidateComponent);
                //放到注册表中
                registry.registerBeanDefinition(beanName, candidateComponent);
            }
        }
        /**@author zzzi
         * @date 2023/11/13 16:20
         * 在这里手动将注解属性填充的工具类注册到注册表中
         * 之所以在这里是因为只有配置了包扫描才有注解属性填充的意义，所以才在这里加入注解属性填充的beanDefinition
         * 如果没有包扫描，那么就不需要注解属性填充，就不需要注册这个beanDefinition
         * 否则就是xml配置的bean，此时只需要正常扫描xml文件，占位符替换即可
         */
        registry.registerBeanDefinition("internalAutowiredAnnotationProcessor",
                new BeanDefinition(AutowiredAnnotationBeanPostProcessor.class));
    }


    //获取到当前bean的作用域
    private String resolveScope(BeanDefinition beanDefinition) {
        Class beanClass = beanDefinition.getBeanClass();
        Scope scope = (Scope) beanClass.getAnnotation(Scope.class);
        if (scope != null)
            return scope.value();
        return "";
    }

    //根据类名获取到当前bean的名称
    private String determineBeanName(BeanDefinition beanDefinition) {
        Class beanClass = beanDefinition.getBeanClass();
        Component component = (Component) beanClass.getAnnotation(Component.class);
        String value = component.value();
        //Component中没有设置bean的名称，那么就是类名小写
        if (value == null || value.equals("")) {
            value = StrUtil.lowerFirst(beanClass.getSimpleName());
        }
        return value;
    }

}
