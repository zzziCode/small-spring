package com.zzzi.springframework.beans.factory.annotation;

import java.lang.annotation.*;

/**
 * @author zzzi
 * @date 2023/11/13 15:20
 * 注入普通属性的注解
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {
    String value();
}
