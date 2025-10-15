package com.windsurf.contractengine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * 会计分录生成响应DTO
 */
@Data
@Schema(description = "会计分录生成响应")
public class JournalEntryGenerationResponse {

    @Schema(description = "合同ID", example = "123")
    private Long contractId;

    @Schema(description = "生成的会计分录列表")
    private List<JournalEntryDto> journalEntries;

    @Schema(description = "汇总信息")
    private SummaryInfo summary;

    @Schema(description = "会计准则说明")
    private AccountingPrinciples accountingPrinciples;

    @Schema(description = "科目映射关系")
    private Map<String, String> glAccountMapping;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Schema(description = "生成时间", example = "2025-01-01T10:00:00Z")
    private ZonedDateTime generatedTime;

    /**
     * 会计分录DTO
     */
    @Data
    @Schema(description = "会计分录")
    public static class JournalEntryDto {
        
        @Schema(description = "分录编号", example = "JE-2025-001")
        private String entryId;

        @Schema(description = "记账日期", example = "2025-01-15")
        private LocalDate bookingDate;

        @Schema(description = "分录描述", example = "合同预付款及费用确认")
        private String description;

        @Schema(description = "参考号", example = "Contract-123-Payment-1")
        private String reference;

        @Schema(description = "分录明细行")
        private List<JournalEntryLineDto> lines;

        @Schema(description = "借方总额", example = "30000.00")
        private BigDecimal totalDr;

        @Schema(description = "贷方总额", example = "30000.00")
        private BigDecimal totalCr;

        @Schema(description = "是否平衡", example = "true")
        private Boolean balanced;
    }

    /**
     * 会计分录明细行DTO
     */
    @Data
    @Schema(description = "会计分录明细行")
    public static class JournalEntryLineDto {
        
        @Schema(description = "行号", example = "1")
        private Integer lineNumber;

        @Schema(description = "记账日期", example = "2025-01-15")
        private LocalDate bookingDate;

        @Schema(description = "科目代码", example = "1221")
        private String glAccount;

        @Schema(description = "科目名称", example = "预付账款")
        private String glAccountName;

        @Schema(description = "借方金额", example = "30000.00")
        private BigDecimal enteredDr;

        @Schema(description = "贷方金额", example = "0.00")
        private BigDecimal enteredCr;

        @Schema(description = "明细描述", example = "确认预付服务费")
        private String description;
    }

    /**
     * 汇总信息
     */
    @Data
    @Schema(description = "汇总信息")
    public static class SummaryInfo {
        
        @Schema(description = "分录总数", example = "3")
        private Integer totalEntries;

        @Schema(description = "付款分录数", example = "1")
        private Integer totalPaymentEntries;

        @Schema(description = "摊销分录数", example = "2")
        private Integer totalAmortizationEntries;

        @Schema(description = "借方总金额", example = "50000.00")
        private BigDecimal totalDrAmount;

        @Schema(description = "贷方总金额", example = "50000.00")
        private BigDecimal totalCrAmount;

        @Schema(description = "合同总金额", example = "120000.00")
        private BigDecimal contractTotalAmount;

        @Schema(description = "已付金额", example = "30000.00")
        private BigDecimal paidAmount;

        @Schema(description = "剩余金额", example = "90000.00")
        private BigDecimal remainingAmount;

        @Schema(description = "已摊销金额", example = "20000.00")
        private BigDecimal amortizedAmount;

        @Schema(description = "预付余额", example = "10000.00")
        private BigDecimal prepaidBalance;
    }

    /**
     * 会计准则说明
     */
    @Data
    @Schema(description = "会计准则说明")
    public static class AccountingPrinciples {
        
        @Schema(description = "付款确认原则", example = "实际付款日期确认预付账款")
        private String paymentRecognition;

        @Schema(description = "费用确认原则", example = "按服务期间摊销确认费用")
        private String expenseRecognition;

        @Schema(description = "平衡规则", example = "借贷必须平衡")
        private String balancingRule;

        @Schema(description = "会计基础", example = "权责发生制")
        private String accrualBasis;
    }
}
