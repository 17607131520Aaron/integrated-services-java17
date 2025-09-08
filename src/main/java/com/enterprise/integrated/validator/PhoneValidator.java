package com.enterprise.integrated.validator;

import com.enterprise.integrated.annotation.validation.Phone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 手机号验证器
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
public class PhoneValidator implements ConstraintValidator<Phone, String> {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    private boolean allowEmpty;

    @Override
    public void initialize(Phone constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return allowEmpty;
        }
        
        return PHONE_PATTERN.matcher(value).matches();
    }
}
