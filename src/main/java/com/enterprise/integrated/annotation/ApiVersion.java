package com.enterprise.integrated.annotation;

import java.lang.annotation.*;

/**
 * API版本控制注解
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiVersion {

    /**
     * 版本号数组
     */
    String[] value() default {"1.0"};
}
