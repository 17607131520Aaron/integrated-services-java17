package com.enterprise.integrated.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.integrated.entity.OperationLog;

/**
 * 操作日志服务接口
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 异步保存操作日志
     */
    void saveAsync(OperationLog operationLog);

    /**
     * 清理过期日志
     */
    void cleanExpiredLogs(int days);
}
