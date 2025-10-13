package com.windsurf.contractengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 摊销计划汇总信息响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "摊销计划汇总信息")
public class AmortizationScheduleSummaryResponse {

    @Schema(description = "总记录数", example = "12")
    private Integer totalRecords;

    @Schema(description = "总摊销金额", example = "120000.00")
    private BigDecimal totalAmortizationAmount;

    @Schema(description = "平均月度金额", example = "10000.00")
    private BigDecimal averageMonthlyAmount;

    @Schema(description = "起始期间", example = "2025-01")
    private String startPeriod;

    @Schema(description = "结束期间", example = "2025-12")
    private String endPeriod;
}
