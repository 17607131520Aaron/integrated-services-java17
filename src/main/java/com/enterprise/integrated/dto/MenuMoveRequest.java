package com.enterprise.integrated.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "菜单移动请求")
public class MenuMoveRequest {

    @NotNull
    private Long id;

    @NotNull
    private Long newParentId;

    @NotNull
    private Integer newSortOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getNewParentId() { return newParentId; }
    public void setNewParentId(Long newParentId) { this.newParentId = newParentId; }
    public Integer getNewSortOrder() { return newSortOrder; }
    public void setNewSortOrder(Integer newSortOrder) { this.newSortOrder = newSortOrder; }
}


