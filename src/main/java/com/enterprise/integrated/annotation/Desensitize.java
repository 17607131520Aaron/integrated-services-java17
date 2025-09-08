package com.enterprise.integrated.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.enterprise.integrated.serializer.DesensitizeSerializer;

import java.lang.annotation.*;

/**
 * 数据脱敏注解
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@JsonSerialize(using = DesensitizeSerializer.class)
public @interface Desensitize {

    /**
     * 脱敏类型
     */
    DesensitizeType type() default DesensitizeType.CUSTOM;

    /**
     * 开始保留长度
     */
    int startLen() default 0;

    /**
     * 结束保留长度
     */
    int endLen() default 0;

    /**
     * 替换字符
     */
    String replacement() default "*";

    /**
     * 脱敏类型枚举
     */
    enum DesensitizeType {
        /**
         * 自定义
         */
        CUSTOM,
        /**
         * 姓名
         */
        NAME,
        /**
         * 手机号
         */
        PHONE,
        /**
         * 身份证号
         */
        ID_CARD,
        /**
         * 邮箱
         */
        EMAIL,
        /**
         * 银行卡号
         */
        BANK_CARD,
        /**
         * 地址
         */
        ADDRESS,
        /**
         * 密码
         */
        PASSWORD
    }
}
