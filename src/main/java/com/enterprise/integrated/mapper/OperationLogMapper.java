package com.enterprise.integrated.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.integrated.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 操作日志Mapper接口
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    IPage<OperationLog> pageOperationLogs(
            IPage<?> page,
            @Param("username") String username,
            @Param("operation") String operation,
            @Param("status") Integer status,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime
    );
}
