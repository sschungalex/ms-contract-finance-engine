package com.windsurf.contractengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 合同列表项响应DTO
 * 用于API 1.2 查询已上传合同列表
 */
@Data
@Schema(description = "合同列表项响应")
public class ContractListItemResponse {

    @Schema(description = "合同ID", example = "123")
    private Long contractId;

    @Schema(description = "原始文件名", example = "contract_abc.pdf")
    private String originalFileName;

    @Schema(description = "合同状态", example = "COMPLETED")
    private String status;

    @Schema(description = "上传时间", example = "2025-01-01T10:00:00Z")
    private LocalDateTime uploadTime;

    @Schema(description = "合同总金额", example = "100000.00")
    private BigDecimal totalAmount;

    @Schema(description = "支付方式", example = "银行转账")
    private String paymentMethod;
}
