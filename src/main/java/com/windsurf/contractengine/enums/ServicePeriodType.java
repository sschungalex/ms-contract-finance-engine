package com.windsurf.contractengine.enums;

/**
 * 服务周期类型枚举
 */
public enum ServicePeriodType {
    ONCE("一次性"),
    WEEKLY("周"),
    MONTHLY("月"),
    QUARTERLY("季度"),
    YEARLY("年");

    private final String description;

    ServicePeriodType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
