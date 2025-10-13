package com.windsurf.contractengine.enums;

/**
 * 分录状态枚举
 */
public enum EntryStatus {
    DRAFT("草稿"),
    POSTED("已过账"),
    CANCELLED("已取消");

    private final String description;

    EntryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
