package com.windsurf.contractengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 摊销计算依据响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "摊销计算依据和公式")
public class AmortizationCalculationBasisResponse {

    @Schema(description = "合同总金额", example = "120000.00")
    private BigDecimal totalAmount;

    @Schema(description = "服务周期类型", example = "MONTHLY")
    private String servicePeriodType;

    @Schema(description = "服务持续时长", example = "12")
    private Integer serviceDuration;

    @Schema(description = "摊销公式", example = "合同总金额 ÷ 服务期间月数")
    private String amortizationFormula;

    @Schema(description = "月度金额计算", example = "120000.00 ÷ 12 = 10000.00")
    private String monthlyAmount;
}
