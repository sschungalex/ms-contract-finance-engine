package com.windsurf.contractengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 合同更新响应DTO - API 1.4
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "合同更新响应")
public class ContractUpdateResponse {

    @Schema(description = "合同ID", example = "123")
    private Long contractId;

    @Schema(description = "响应消息", example = "合同数据更新成功")
    private String message;

    @Schema(description = "更新时间", example = "2025-01-01T10:30:00Z")
    private LocalDateTime updatedTime;

    @Schema(description = "已更新的字段列表", example = "[\"paymentMethod\", \"totalAmount\", \"paymentDates\", \"taxRate\"]")
    private List<String> updatedFields;
}
