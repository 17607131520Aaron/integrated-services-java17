package com.enterprise.integrated.common.exception;

import com.enterprise.integrated.common.result.ResultCode;

/**
 * 业务异常
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
public class BusinessException extends RuntimeException {
    
    private final ResultCode resultCode;
    private final Object data;
    private final BusinessExceptionCode businessCode;
    
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
        this.data = null;
        this.businessCode = null;
    }
    
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
        this.data = null;
        this.businessCode = null;
    }
    
    public BusinessException(ResultCode resultCode, String message, Object data) {
        super(message);
        this.resultCode = resultCode;
        this.data = data;
        this.businessCode = null;
    }
    
    public BusinessException(ResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.resultCode = resultCode;
        this.data = null;
        this.businessCode = null;
    }
    
    public BusinessException(BusinessExceptionCode businessCode) {
        super(businessCode.getMessage());
        this.resultCode = ResultCode.BUSINESS_ERROR;
        this.data = null;
        this.businessCode = businessCode;
    }
    
    public BusinessException(BusinessExceptionCode businessCode, String message) {
        super(message);
        this.resultCode = ResultCode.BUSINESS_ERROR;
        this.data = null;
        this.businessCode = businessCode;
    }
    
    public BusinessException(BusinessExceptionCode businessCode, String message, Object data) {
        super(message);
        this.resultCode = ResultCode.BUSINESS_ERROR;
        this.data = data;
        this.businessCode = businessCode;
    }
    
    public ResultCode getResultCode() {
        return resultCode;
    }
    
    public Object getData() {
        return data;
    }
    
    public BusinessExceptionCode getBusinessCode() {
        return businessCode;
    }
    
    public int getBusinessCodeValue() {
        return businessCode != null ? businessCode.getCode() : resultCode.getCode();
    }
}
