package com.enterprise.integrated.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "创建菜单请求")
public class MenuCreateRequest {

    @Schema(description = "父菜单ID，根节点为0")
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    @Schema(description = "菜单名称")
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称不能超过50个字符")
    private String name;

    @Schema(description = "菜单编码（唯一）")
    @NotBlank(message = "菜单编码不能为空")
    @Size(max = 100, message = "菜单编码不能超过100个字符")
    private String code;

    @Schema(description = "菜单路径")
    @Size(max = 200, message = "路径不能超过200个字符")
    private String path;

    @Schema(description = "排序")
    private Integer sortOrder;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}


