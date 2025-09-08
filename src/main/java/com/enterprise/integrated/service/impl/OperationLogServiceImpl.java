package com.enterprise.integrated.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.integrated.entity.OperationLog;
import com.enterprise.integrated.mapper.OperationLogMapper;
import com.enterprise.integrated.service.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    private static final Logger log = LoggerFactory.getLogger(OperationLogServiceImpl.class);

    @Override
    @Async
    public void saveAsync(OperationLog operationLog) {
        try {
            this.save(operationLog);
        } catch (Exception e) {
            log.error("异步保存操作日志失败", e);
        }
    }

    @Override
    public void cleanExpiredLogs(int days) {
        try {
            LocalDateTime expiredTime = LocalDateTime.now().minusDays(days);
            LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.lt(OperationLog::getCreateTime, expiredTime);
            
            long count = this.count(queryWrapper);
            if (count > 0) {
                this.remove(queryWrapper);
                log.info("清理过期操作日志 {} 条", count);
            }
        } catch (Exception e) {
            log.error("清理过期操作日志失败", e);
        }
    }
}
