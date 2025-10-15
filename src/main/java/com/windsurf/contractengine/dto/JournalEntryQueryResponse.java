package com.windsurf.contractengine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会计分录查询响应DTO
 */
@Data
@Schema(description = "会计分录查询响应")
public class JournalEntryQueryResponse {

    @Schema(description = "合同ID", example = "123")
    private Long contractId;

    @Schema(description = "会计分录列表")
    private List<JournalEntryItemDto> journalEntries;

    @Schema(description = "汇总统计信息")
    private JournalEntrySummaryDto summary;

    /**
     * 会计分录项DTO
     */
    @Data
    @Schema(description = "会计分录项")
    public static class JournalEntryItemDto {
        
        @Schema(description = "数据库ID", example = "1")
        private Long id;

        @Schema(description = "分录编号", example = "JE-2025-001")
        private String entryNumber;

        @Schema(description = "分录ID", example = "JE-2025-001")
        private String entryId;

        @Schema(description = "分录日期", example = "2025-01-15")
        private LocalDate entryDate;

        @Schema(description = "记账日期", example = "2025-01-15")
        private LocalDate bookingDate;

        @Schema(description = "分录类型", example = "PAYMENT")
        private String entryType;

        @Schema(description = "分录描述", example = "合同预付款确认")
        private String description;

        @Schema(description = "参考号", example = "Contract-123-Payment-1")
        private String reference;

        @Schema(description = "总金额", example = "30000.00")
        private BigDecimal totalAmount;

        @Schema(description = "借方总额", example = "30000.00")
        private BigDecimal totalDr;

        @Schema(description = "贷方总额", example = "30000.00")
        private BigDecimal totalCr;

        @Schema(description = "是否平衡", example = "true")
        private Boolean balanced;

        @Schema(description = "分录状态", example = "POSTED")
        private String status;

        @Schema(description = "关联的支付计划ID", example = "1")
        private Long paymentScheduleId;

        @Schema(description = "关联的摊销计划ID", example = "1")
        private Long amortizationScheduleId;

        @Schema(description = "备注", example = "系统自动生成")
        private String remarks;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "创建时间", example = "2025-01-15 10:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "创建人", example = "system")
        private String createdBy;

        @Schema(description = "分录明细行")
        private List<JournalEntryLineItemDto> entryLines;
    }

    /**
     * 会计分录明细行项DTO
     */
    @Data
    @Schema(description = "会计分录明细行项")
    public static class JournalEntryLineItemDto {
        
        @Schema(description = "数据库ID", example = "1")
        private Long id;

        @Schema(description = "行号", example = "1")
        private Integer lineNumber;

        @Schema(description = "记账日期", example = "2025-01-15")
        private LocalDate bookingDate;

        @Schema(description = "GL科目代码", example = "1221")
        private String glAccount;

        @Schema(description = "GL科目名称", example = "预付账款")
        private String glAccountName;

        @Schema(description = "借方金额", example = "30000.00")
        private BigDecimal debitAmount;

        @Schema(description = "贷方金额", example = "0.00")
        private BigDecimal creditAmount;

        @Schema(description = "录入借方金额", example = "30000.00")
        private BigDecimal enteredDr;

        @Schema(description = "录入贷方金额", example = "0.00")
        private BigDecimal enteredCr;

        @Schema(description = "摘要", example = "确认预付服务费")
        private String description;

        @Schema(description = "辅助核算信息")
        private String auxiliaryInfo;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "创建时间", example = "2025-01-15 10:30:00")
        private LocalDateTime createdAt;
    }

    /**
     * 会计分录汇总统计DTO
     */
    @Data
    @Schema(description = "会计分录汇总统计")
    public static class JournalEntrySummaryDto {
        
        @Schema(description = "分录总数", example = "5")
        private Integer totalEntries;

        @Schema(description = "付款分录数", example = "2")
        private Integer paymentEntries;

        @Schema(description = "摊销分录数", example = "3")
        private Integer amortizationEntries;

        @Schema(description = "借方总金额", example = "150000.00")
        private BigDecimal totalDebitAmount;

        @Schema(description = "贷方总金额", example = "150000.00")
        private BigDecimal totalCreditAmount;

        @Schema(description = "平衡状态", example = "true")
        private Boolean balanced;

        @Schema(description = "最早分录日期", example = "2025-01-15")
        private LocalDate earliestEntryDate;

        @Schema(description = "最晚分录日期", example = "2025-12-31")
        private LocalDate latestEntryDate;

        @Schema(description = "草稿状态分录数", example = "0")
        private Integer draftEntries;

        @Schema(description = "已过账分录数", example = "5")
        private Integer postedEntries;
    }
}
