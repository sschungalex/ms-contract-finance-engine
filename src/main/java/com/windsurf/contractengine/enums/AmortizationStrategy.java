package com.windsurf.contractengine.enums;

/**
 * 摊销策略枚举
 */
public enum AmortizationStrategy {
    /**
     * 按服务周期均匀摊销
     */
    SERVICE_PERIOD("按服务周期均匀摊销"),
    
    /**
     * 按交付节点百分比摊销
     */
    DELIVERY_NODES("按交付节点百分比摊销");

    private final String description;

    AmortizationStrategy(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
