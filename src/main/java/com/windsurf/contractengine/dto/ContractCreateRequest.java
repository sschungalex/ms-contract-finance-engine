package com.windsurf.contractengine.dto;

import com.windsurf.contractengine.entity.Contract;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 合同创建请求DTO
 */
@Data
@Schema(description = "合同创建请求")
public class ContractCreateRequest {

    @NotBlank(message = "合同名称不能为空")
    @Size(max = 200, message = "合同名称长度不能超过200字符")
    @Schema(description = "合同名称", example = "软件开发服务合同")
    private String contractName;

    @NotBlank(message = "合同类型不能为空")
    @Size(max = 50, message = "合同类型长度不能超过50字符")
    @Schema(description = "合同类型", example = "服务合同")
    private String contractType;

    @NotBlank(message = "合同对方不能为空")
    @Size(max = 200, message = "合同对方长度不能超过200字符")
    @Schema(description = "合同对方", example = "ABC科技有限公司")
    private String counterparty;

    @NotNull(message = "合同开始日期不能为空")
    @Schema(description = "合同开始日期", example = "2024-01-01T00:00:00")
    private LocalDateTime startDate;

    @NotNull(message = "合同结束日期不能为空")
    @Schema(description = "合同结束日期", example = "2024-12-31T23:59:59")
    private LocalDateTime endDate;

    @NotNull(message = "合同总金额不能为空")
    @DecimalMin(value = "0.01", message = "合同总金额必须大于0")
    @Digits(integer = 13, fraction = 2, message = "金额格式不正确")
    @Schema(description = "合同总金额", example = "100000.00")
    private BigDecimal totalAmount;

    @Size(max = 10, message = "币种长度不能超过10字符")
    @Schema(description = "币种", example = "CNY")
    private String currency = "CNY";

    @NotNull(message = "支付频率不能为空")
    @Schema(description = "支付频率", example = "MONTHLY")
    private Contract.PaymentFrequency paymentFrequency;

    @Size(max = 50, message = "支付方式长度不能超过50字符")
    @Schema(description = "支付方式", example = "银行转账")
    private String paymentMethod;

    @Size(max = 100, message = "创建人长度不能超过100字符")
    @Schema(description = "创建人", example = "张三")
    private String createdBy;
}
