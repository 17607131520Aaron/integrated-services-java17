package com.enterprise.integrated.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作描述
     */
    String value() default "";

    /**
     * 操作类型
     */
    OperationType type() default OperationType.OTHER;

    /**
     * 是否记录请求参数
     */
    boolean recordParams() default true;

    /**
     * 是否记录返回结果
     */
    boolean recordResult() default false;

    /**
     * 操作类型枚举
     */
    enum OperationType {
        /**
         * 查询
         */
        SELECT("查询"),
        /**
         * 新增
         */
        INSERT("新增"),
        /**
         * 修改
         */
        UPDATE("修改"),
        /**
         * 删除
         */
        DELETE("删除"),
        /**
         * 登录
         */
        LOGIN("登录"),
        /**
         * 登出
         */
        LOGOUT("登出"),
        /**
         * 其他
         */
        OTHER("其他");

        private final String description;

        OperationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
