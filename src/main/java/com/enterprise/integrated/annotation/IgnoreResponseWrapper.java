package com.enterprise.integrated.annotation;

import java.lang.annotation.*;

/**
 * 忽略全局响应包装注解
 * 用于标记不需要统一包装的接口
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreResponseWrapper {
}
