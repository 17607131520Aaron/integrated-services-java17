package com.enterprise.integrated.common.result;

/**
 * 统一返回结果状态码枚举
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
public enum ResultCode {

    // 成功
    SUCCESS(0, "操作成功"),

    // 默认异常
    ERROR(9000, "操作失败"),
    
    // 客户端错误 4xx
    BAD_REQUEST(400, "请求错误"),
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(400, "缺少必要参数"),
    PARAM_INVALID(400, "参数格式不正确"),
    
    UNAUTHORIZED(401, "未授权访问"),
    TOKEN_INVALID(401, "Token无效"),
    TOKEN_EXPIRED(401, "Token已过期"),
    
    FORBIDDEN(403, "访问被禁止"),
    PERMISSION_DENIED(403, "权限不足"),
    
    NOT_FOUND(404, "资源不存在"),
    
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    CONFLICT(409, "资源冲突"),
    DATA_ALREADY_EXISTS(409, "数据已存在"),
    
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // 服务端错误 5xx
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),
    
    // 业务错误 6xxx
    BUSINESS_ERROR(6000, "业务处理失败"),
    DATA_NOT_FOUND(6001, "数据不存在"),
    DATA_INVALID(6002, "数据无效"),
    OPERATION_FAILED(6003, "操作失败"),
    
    // 数据库错误 7xxx
    DATABASE_ERROR(7000, "数据库操作失败"),
    DATABASE_CONNECTION_ERROR(7001, "数据库连接失败"),
    
    // 外部服务错误 (70xx)
    EXTERNAL_SERVICE_ERROR(7002, "外部服务调用失败"),
    EXTERNAL_SERVICE_TIMEOUT(7003, "外部服务调用超时"),
    EXTERNAL_SERVICE_UNAVAILABLE(7004, "外部服务不可用"),
    
    // 缓存错误 8xxx
    CACHE_ERROR(8001, "缓存操作失败"),
    
    // 网络错误 9xxx
    NETWORK_ERROR(9001, "网络连接异常");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据状态码获取枚举
     */
    public static ResultCode getByCode(Integer code) {
        for (ResultCode resultCode : values()) {
            if (resultCode.getCode().equals(code)) {
                return resultCode;
            }
        }
        return ERROR;
    }
}
