package com.windsurf.contractengine.enums;

/**
 * 分录类型枚举
 */
public enum EntryType {
    PAYMENT("支付分录"),
    AMORTIZATION("摊销分录"),
    VARIANCE("差额分录"),
    ADJUSTMENT("调整分录");

    private final String description;

    EntryType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
