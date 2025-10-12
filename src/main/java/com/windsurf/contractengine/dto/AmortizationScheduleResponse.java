package com.windsurf.contractengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 摊销计划响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "摊销计划完整响应")
public class AmortizationScheduleResponse {

    @Schema(description = "合同ID", example = "123")
    private Long contractId;

    @Schema(description = "摊销策略", example = "SERVICE_PERIOD")
    private String amortizationStrategy;

    @Schema(description = "合同基础信息")
    private AmortizationContractInfoResponse contractInfo;

    @Schema(description = "摊销计划明细列表")
    private List<AmortizationScheduleItemResponse> amortizationSchedule;

    @Schema(description = "摊销计划汇总信息")
    private AmortizationScheduleSummaryResponse summary;

    @Schema(description = "生成时间", example = "2025-01-01T10:00:00Z")
    private LocalDateTime generatedTime;

    @Schema(description = "计算依据和公式")
    private AmortizationCalculationBasisResponse calculationBasis;
}
