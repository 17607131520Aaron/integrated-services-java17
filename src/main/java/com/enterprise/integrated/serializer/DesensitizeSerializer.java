package com.enterprise.integrated.serializer;

import com.enterprise.integrated.annotation.Desensitize;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;
import java.util.Objects;

/**
 * 数据脱敏序列化器
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
public class DesensitizeSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private Desensitize.DesensitizeType type;
    private int startLen;
    private int endLen;
    private String replacement;

    public DesensitizeSerializer() {
    }

    public DesensitizeSerializer(Desensitize.DesensitizeType type, int startLen, int endLen, String replacement) {
        this.type = type;
        this.startLen = startLen;
        this.endLen = endLen;
        this.replacement = replacement;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(desensitize(value));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        Desensitize annotation = property.getAnnotation(Desensitize.class);
        if (Objects.nonNull(annotation)) {
            return new DesensitizeSerializer(annotation.type(), annotation.startLen(), annotation.endLen(), annotation.replacement());
        }
        return prov.findValueSerializer(property.getType(), property);
    }

    /**
     * 脱敏处理
     */
    private String desensitize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        switch (type) {
            case NAME:
                return desensitizeName(value);
            case PHONE:
                return desensitizePhone(value);
            case ID_CARD:
                return desensitizeIdCard(value);
            case EMAIL:
                return desensitizeEmail(value);
            case BANK_CARD:
                return desensitizeBankCard(value);
            case ADDRESS:
                return desensitizeAddress(value);
            case PASSWORD:
                return desensitizePassword(value);
            case CUSTOM:
            default:
                return desensitizeCustom(value);
        }
    }

    /**
     * 姓名脱敏
     */
    private String desensitizeName(String name) {
        if (name.length() <= 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    /**
     * 手机号脱敏
     */
    private String desensitizePhone(String phone) {
        if (phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 身份证号脱敏
     */
    private String desensitizeIdCard(String idCard) {
        if (idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 邮箱脱敏
     */
    private String desensitizeEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return email;
        }
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (username.length() <= 2) {
            return username.charAt(0) + "*" + domain;
        }
        return username.charAt(0) + "*".repeat(username.length() - 2) + username.charAt(username.length() - 1) + domain;
    }

    /**
     * 银行卡号脱敏
     */
    private String desensitizeBankCard(String bankCard) {
        if (bankCard.length() < 8) {
            return bankCard;
        }
        return bankCard.substring(0, 4) + " **** **** " + bankCard.substring(bankCard.length() - 4);
    }

    /**
     * 地址脱敏
     */
    private String desensitizeAddress(String address) {
        if (address.length() <= 6) {
            return address;
        }
        return address.substring(0, 6) + "****";
    }

    /**
     * 密码脱敏
     */
    private String desensitizePassword(String password) {
        return "******";
    }

    /**
     * 自定义脱敏
     */
    private String desensitizeCustom(String value) {
        int length = value.length();
        int start = Math.min(startLen, length);
        int end = Math.min(endLen, length - start);
        
        if (start + end >= length) {
            return value;
        }
        
        String startStr = value.substring(0, start);
        String endStr = value.substring(length - end);
        String middleStr = replacement.repeat(length - start - end);
        
        return startStr + middleStr + endStr;
    }
}
