package com.zzzi.springframework.context.annotation;

import cn.hutool.core.util.ClassUtil;
import com.zzzi.springframework.beans.factory.config.BeanDefinition;
import com.zzzi.springframework.sterotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @author zzzi
 * @date 2023/11/12 14:46
 * 在这里进行包扫描，根据传递来的包扫描路径得到这个路径下所有使用了Component注解的类
 */
public class ClassPathScanningCandidateComponentProvider {

    public Set<BeanDefinition> findCandidateComponents(String basePackage) {
        Set<BeanDefinition> candidates = new HashSet<>();
        //调用hutool包中的方法得到路径下的所有使用Component的类信息
        //核心就是这个代码，得到所有的类型西周，将其保存到BeanDefinition中
        Set<Class<?>> classes = ClassUtil.scanPackageByAnnotation(basePackage, Component.class);
        for (Class<?> clazz : classes) {
            candidates.add(new BeanDefinition(clazz));
        }
        return candidates;
    }
}
