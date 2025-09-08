package com.enterprise.integrated.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.integrated.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统配置Mapper接口
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    IPage<SystemConfig> pageConfigs(
            IPage<?> page,
            @Param("configKey") String configKey,
            @Param("type") String type,
            @Param("status") Integer status
    );

    SystemConfig findByConfigKey(@Param("configKey") String configKey);
}
