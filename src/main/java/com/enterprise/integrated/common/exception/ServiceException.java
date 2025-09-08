package com.enterprise.integrated.common.exception;

import com.enterprise.integrated.common.result.ResultCode;

/**
 * 业务服务异常类
 * 支持自定义状态码和消息
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码，默认为9000
     */
    private Integer code = 9000;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 附加数据
     */
    private Object data;

    public ServiceException() {
        super();
        this.code = 9000;
        this.message = "服务异常";
    }

    public ServiceException(String message) {
        super(message);
        this.code = 9000;
        this.message = message;
    }

    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public ServiceException(Integer code, String message, Object data) {
        super(message);
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ServiceException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public ServiceException(ResultCode resultCode, Object data) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.code = 9000;
        this.message = message;
    }

    public ServiceException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    // 静态工厂方法
    public static ServiceException of(String message) {
        return new ServiceException(message);
    }

    public static ServiceException of(Integer code, String message) {
        return new ServiceException(code, message);
    }

    public static ServiceException of(ResultCode resultCode) {
        return new ServiceException(resultCode);
    }

    public static ServiceException of(ResultCode resultCode, Object data) {
        return new ServiceException(resultCode, data);
    }

    // Getters and Setters
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
