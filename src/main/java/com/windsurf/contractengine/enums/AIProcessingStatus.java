package com.windsurf.contractengine.enums;

/**
 * AI处理状态枚举
 */
public enum AIProcessingStatus {
    PROCESSING("处理中"),
    COMPLETED("已完成"),
    FAILED("处理失败");

    private final String description;

    AIProcessingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
