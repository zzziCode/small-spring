package com.zzzi.springframework.sterotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zzzi
 * @date 2023/11/12 14:13
 * 在这里定义一个注解，使用这个注解的类认为其成为一个bean
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
    //这里可以设置bean的名称
    String value() default "";
}
