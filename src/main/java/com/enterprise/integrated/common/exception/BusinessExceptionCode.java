package com.enterprise.integrated.common.exception;

/**
 * 业务异常代码枚举
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
public enum BusinessExceptionCode {
    
    // 用户相关异常 1000-1999
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_PASSWORD_ERROR(1004, "密码错误"),
    USER_ACCOUNT_LOCKED(1005, "账户已被锁定"),
    USER_ACCOUNT_EXPIRED(1006, "账户已过期"),
    
    // 权限相关异常 2000-2999
    PERMISSION_DENIED(2001, "权限不足"),
    ROLE_NOT_FOUND(2002, "角色不存在"),
    RESOURCE_ACCESS_DENIED(2003, "资源访问被拒绝"),
    
    // 业务逻辑异常 3000-3999
    BUSINESS_LOGIC_ERROR(3001, "业务逻辑错误"),
    DATA_NOT_FOUND(3002, "数据不存在"),
    DATA_ALREADY_EXISTS(3003, "数据已存在"),
    DATA_STATUS_ERROR(3004, "数据状态错误"),
    OPERATION_NOT_ALLOWED(3005, "操作不被允许"),
    
    // 系统配置异常 4000-4999
    CONFIG_NOT_FOUND(4001, "配置不存在"),
    CONFIG_VALUE_INVALID(4002, "配置值无效"),
    
    // 第三方服务异常 5000-5999
    THIRD_PARTY_SERVICE_ERROR(5001, "第三方服务异常"),
    THIRD_PARTY_SERVICE_TIMEOUT(5002, "第三方服务超时"),
    THIRD_PARTY_SERVICE_UNAVAILABLE(5003, "第三方服务不可用"),
    
    // 文件操作异常 6000-6999
    FILE_NOT_FOUND(6001, "文件不存在"),
    FILE_UPLOAD_FAILED(6002, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(6003, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(6004, "文件大小超限"),
    
    // 缓存异常 7000-7999
    CACHE_ERROR(7001, "缓存异常"),
    CACHE_KEY_NOT_FOUND(7002, "缓存键不存在"),
    
    // 分布式锁异常 8000-8999
    LOCK_ACQUIRE_FAILED(8001, "获取锁失败"),
    LOCK_RELEASE_FAILED(8002, "释放锁失败"),
    LOCK_TIMEOUT(8003, "锁超时");
    
    private final int code;
    private final String message;
    
    BusinessExceptionCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
