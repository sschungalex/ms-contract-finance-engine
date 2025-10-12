package com.windsurf.contractengine.enums;

/**
 * 摊销状态枚举
 */
public enum AmortizationStatus {
    PENDING("待摊销"),
    PROCESSED("已摊销"),
    CANCELLED("已取消");

    private final String description;

    AmortizationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
