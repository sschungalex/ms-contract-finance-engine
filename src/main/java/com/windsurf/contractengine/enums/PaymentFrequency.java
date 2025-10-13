package com.windsurf.contractengine.enums;

/**
 * 支付频率枚举
 */
public enum PaymentFrequency {
    MONTHLY("月付"),
    QUARTERLY("季付"),
    SEMI_ANNUALLY("半年付"),
    ANNUALLY("年付"),
    ONE_TIME("一次性");

    private final String description;

    PaymentFrequency(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
