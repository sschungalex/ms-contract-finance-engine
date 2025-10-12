package com.windsurf.contractengine.dto;

import com.windsurf.contractengine.enums.ContractStatus;
import com.windsurf.contractengine.enums.PaymentFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 合同响应DTO
 */
@Data
@Schema(description = "合同响应")
public class ContractResponse {

    @Schema(description = "合同ID", example = "1")
    private Long id;

    @Schema(description = "合同编号", example = "CT202401001")
    private String contractNumber;

    @Schema(description = "合同名称", example = "软件开发服务合同")
    private String contractName;

    @Schema(description = "合同类型", example = "服务合同")
    private String contractType;

    @Schema(description = "合同对方", example = "ABC科技有限公司")
    private String counterparty;

    @Schema(description = "合同开始日期", example = "2024-01-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "合同结束日期", example = "2024-12-31T23:59:59")
    private LocalDateTime endDate;

    @Schema(description = "合同状态", example = "ACTIVE")
    private ContractStatus status;

    @Schema(description = "合同总金额", example = "100000.00")
    private BigDecimal totalAmount;

    @Schema(description = "币种", example = "CNY")
    private String currency;

    @Schema(description = "支付频率", example = "MONTHLY")
    private PaymentFrequency paymentFrequency;

    @Schema(description = "支付方式", example = "银行转账")
    private String paymentMethod;

    @Schema(description = "原始文件名", example = "contract.pdf")
    private String originalFilename;

    @Schema(description = "文件大小", example = "1024000")
    private Long fileSize;

    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "创建人", example = "张三")
    private String createdBy;
}
