package com.enterprise.integrated.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.integrated.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 系统配置实体
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@TableName("sys_config")
@Schema(description = "系统配置")
public class SystemConfig extends BaseEntity {

    @Schema(description = "配置键")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "配置描述")
    private String description;

    @Schema(description = "配置类型")
    private String type;

    @Schema(description = "状态(0:禁用 1:启用)")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    // Getter and Setter methods
    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
