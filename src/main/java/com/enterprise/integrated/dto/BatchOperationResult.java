package com.enterprise.integrated.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "批量操作结果")
public class BatchOperationResult {

    private int totalCount;
    private int successCount;
    private int skippedCount;
    private int failedCount;
    private List<Long> invalidIds = new ArrayList<>();
    private List<Long> duplicatedIds = new ArrayList<>();
    private List<String> errorMessages = new ArrayList<>();

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public int getSkippedCount() { return skippedCount; }
    public void setSkippedCount(int skippedCount) { this.skippedCount = skippedCount; }
    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
    public List<Long> getInvalidIds() { return invalidIds; }
    public void setInvalidIds(List<Long> invalidIds) { this.invalidIds = invalidIds; }
    public List<Long> getDuplicatedIds() { return duplicatedIds; }
    public void setDuplicatedIds(List<Long> duplicatedIds) { this.duplicatedIds = duplicatedIds; }
    public List<String> getErrorMessages() { return errorMessages; }
    public void setErrorMessages(List<String> errorMessages) { this.errorMessages = errorMessages; }

    public void addError(String message) {
        if (message != null) {
            this.errorMessages.add(message);
        }
    }
}


