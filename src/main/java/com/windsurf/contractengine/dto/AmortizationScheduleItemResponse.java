package com.windsurf.contractengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 摊销计划明细项响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "摊销计划明细项")
public class AmortizationScheduleItemResponse {

    @Schema(description = "预提/摊销期间", example = "2025-01")
    private String period;

    @Schema(description = "预提期间详细描述", example = "2025-01-01 至 2025-01-31")
    private String accrualPeriod;

    @Schema(description = "入账期间", example = "2025-01")
    private String accountingPeriod;

    @Schema(description = "预提/摊销金额", example = "10000.00")
    private BigDecimal amortizationAmount;

    @Schema(description = "里程碑名称（交付节点策略时使用）", example = "项目启动")
    private String milestone;

    @Schema(description = "摊销百分比（交付节点策略时使用）", example = "30")
    private Integer percentage;
}
