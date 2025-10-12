package com.windsurf.contractengine.enums;

/**
 * 支付状态枚举
 */
public enum PaymentStatus {
    PENDING("待支付"),
    PAID("已支付"),
    OVERDUE("逾期"),
    PARTIAL("部分支付"),
    CANCELLED("已取消");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
