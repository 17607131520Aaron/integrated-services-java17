package com.enterprise.integrated.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.integrated.entity.SystemConfig;

/**
 * 系统配置服务接口
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
public interface SystemConfigService extends IService<SystemConfig> {

    /**
     * 根据配置键获取配置值
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);

    /**
     * 根据配置键获取配置值，如果不存在返回默认值
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * 更新配置值
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 是否更新成功
     */
    boolean updateConfigValue(String configKey, String configValue);

    /**
     * 刷新配置缓存
     */
    void refreshCache();
}
