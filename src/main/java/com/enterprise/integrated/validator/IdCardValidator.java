package com.enterprise.integrated.validator;

import com.enterprise.integrated.annotation.validation.IdCard;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 身份证号验证器
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
public class IdCardValidator implements ConstraintValidator<IdCard, String> {

    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$");
    
    private boolean allowEmpty;

    @Override
    public void initialize(IdCard constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return allowEmpty;
        }
        
        if (!ID_CARD_PATTERN.matcher(value).matches()) {
            return false;
        }
        
        // 验证校验码
        return validateCheckCode(value);
    }
    
    /**
     * 验证身份证校验码
     */
    private boolean validateCheckCode(String idCard) {
        if (idCard.length() != 18) {
            return false;
        }
        
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] checkCodes = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (idCard.charAt(i) - '0') * weights[i];
        }
        
        char checkCode = checkCodes[sum % 11];
        return checkCode == Character.toUpperCase(idCard.charAt(17));
    }
}
