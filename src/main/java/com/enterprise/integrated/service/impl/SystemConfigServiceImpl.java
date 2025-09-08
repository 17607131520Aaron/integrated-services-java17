package com.enterprise.integrated.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.integrated.entity.SystemConfig;
import com.enterprise.integrated.mapper.SystemConfigMapper;
import com.enterprise.integrated.service.SystemConfigService;
import com.enterprise.integrated.utils.CacheUtils;
import org.springframework.stereotype.Service;

/**
 * 系统配置服务实现
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    private final CacheUtils cacheUtils;
    private static final String CONFIG_CACHE_PREFIX = "system_config:";

    public SystemConfigServiceImpl(CacheUtils cacheUtils) {
        this.cacheUtils = cacheUtils;
    }

    @Override
    public String getConfigValue(String configKey) {
        return getConfigValue(configKey, null);
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        // 先从缓存获取
        String cacheKey = CONFIG_CACHE_PREFIX + configKey;
        Object cachedValue = cacheUtils.get(cacheKey);
        if (cachedValue != null) {
            return cachedValue.toString();
        }

        // 从数据库获取
        LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConfig::getConfigKey, configKey)
                   .eq(SystemConfig::getStatus, 1);
        
        SystemConfig config = this.getOne(queryWrapper);
        if (config != null) {
            // 存入缓存，缓存1小时
            cacheUtils.set(cacheKey, config.getConfigValue(), 3600);
            return config.getConfigValue();
        }

        return defaultValue;
    }

    @Override
    public boolean updateConfigValue(String configKey, String configValue) {
        LambdaUpdateWrapper<SystemConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SystemConfig::getConfigKey, configKey)
                    .set(SystemConfig::getConfigValue, configValue);
        
        boolean success = this.update(updateWrapper);
        if (success) {
            // 更新缓存
            String cacheKey = CONFIG_CACHE_PREFIX + configKey;
            cacheUtils.set(cacheKey, configValue, 3600);
        }
        
        return success;
    }

    @Override
    public void refreshCache() {
        // 清除所有配置缓存
        // 这里可以根据实际需求实现更精确的缓存清理
        // 由于Redis没有直接的通配符删除，这里简化处理
        // 实际项目中可以维护一个配置键列表来精确清理
    }
}
