package com.windsurf.contractengine.enums;

/**
 * 合同状态枚举
 */
public enum ContractStatus {
    DRAFT("草稿"),
    ACTIVE("生效"),
    EXPIRED("过期"),
    TERMINATED("终止");

    private final String description;

    ContractStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
