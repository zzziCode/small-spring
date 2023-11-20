package com.zzzi.springframework.beans.factory.annotation;

import java.lang.annotation.*;

/**
 * @author zzzi
 * @date 2023/11/13 15:23
 * 当一个类型下有几个bean时，用来指定注入哪一个bean
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Qualifier {
    String value() default "";
}
