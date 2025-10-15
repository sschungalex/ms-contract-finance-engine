package com.windsurf.contractengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 会计分录生成请求DTO
 */
@Data
@Schema(description = "会计分录生成请求")
public class JournalEntryGenerationRequest {

    @Valid
    @NotNull(message = "合同信息不能为空")
    @Schema(description = "合同基础信息", required = true)
    private ContractInfo contractInfo;

    @Valid
    @Size(max = 50, message = "付款记录最多50笔")
    @Schema(description = "实际付款信息列表，如果为空则使用现有摊销记录生成会计分录")
    private List<ActualPayment> actualPayments;

    @Valid
    @Schema(description = "生成选项")
    private GenerateOptions generateOptions;

    /**
     * 合同基础信息
     */
    @Data
    @Schema(description = "合同基础信息")
    public static class ContractInfo {
        
        @NotNull(message = "合同ID不能为空")
        @Positive(message = "合同ID必须大于0")
        @Schema(description = "合同ID", required = true, example = "123")
        private Long contractId;

        @NotNull(message = "合同总金额不能为空")
        @DecimalMin(value = "0.01", message = "合同总金额必须大于0")
        @Schema(description = "合同总金额", required = true, example = "120000.00")
        private BigDecimal totalAmount;

        @Valid
        @NotNull(message = "付款周期信息不能为空")
        @Schema(description = "付款周期信息", required = true)
        private PaymentPeriod paymentPeriod;
    }

    /**
     * 付款周期信息
     */
    @Data
    @Schema(description = "付款周期信息")
    public static class PaymentPeriod {
        
        @NotBlank(message = "付款周期类型不能为空")
        @Pattern(regexp = "ONCE|WEEKLY|MONTHLY|QUARTERLY|YEARLY", 
                message = "付款周期类型必须是ONCE、WEEKLY、MONTHLY、QUARTERLY或YEARLY")
        @Schema(description = "付款周期类型", required = true, example = "MONTHLY", 
                allowableValues = {"ONCE", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY"})
        private String type;

        @NotNull(message = "持续时长不能为空")
        @Positive(message = "持续时长必须大于0")
        @Schema(description = "持续时长", required = true, example = "12")
        private Integer duration;

        @Size(max = 200, message = "描述信息最大200字符")
        @Schema(description = "描述信息", example = "按月服务，共12个月")
        private String description;
    }

    /**
     * 实际付款信息
     */
    @Data
    @Schema(description = "实际付款信息")
    public static class ActualPayment {
        
        @NotNull(message = "付款日期不能为空")
        @Schema(description = "实际付款日期", required = true, example = "2025-01-15")
        private LocalDate paymentDate;

        @NotNull(message = "付款金额不能为空")
        @DecimalMin(value = "0.01", message = "付款金额必须大于0")
        @Schema(description = "实际付款金额", required = true, example = "30000.00")
        private BigDecimal amount;

        @NotBlank(message = "付款方式不能为空")
        @Size(max = 50, message = "付款方式最大50字符")
        @Schema(description = "付款方式", required = true, example = "银行转账")
        private String paymentMethod;

        @Size(max = 200, message = "付款描述最大200字符")
        @Schema(description = "付款描述", example = "首期付款")
        private String description;
    }

    /**
     * 生成选项
     */
    @Data
    @Schema(description = "生成选项")
    public static class GenerateOptions {
        
        @Schema(description = "是否包含预提分录", example = "true")
        private Boolean includeAccruals = true;

        @Schema(description = "是否用费用科目平衡差额", example = "true")
        private Boolean autoBalanceWithExpense = true;

        @Size(max = 10, message = "基础货币代码最大10字符")
        @Schema(description = "基础货币", example = "CNY")
        private String baseCurrency = "CNY";
    }
}
