package com.windsurf.contractengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 摊销计划中的合同基础信息响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "合同基础信息")
public class AmortizationContractInfoResponse {

    @Schema(description = "文件名", example = "contract_abc.pdf")
    private String fileName;

    @Schema(description = "合同总金额", example = "120000.00")
    private BigDecimal totalAmount;

    @Schema(description = "合同开始日期", example = "2025-01-01")
    private String contractStartDate;

    @Schema(description = "服务周期信息")
    private ServicePeriodResponse servicePeriod;

    /**
     * 服务周期信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "服务周期信息")
    public static class ServicePeriodResponse {

        @Schema(description = "服务周期类型", example = "MONTHLY")
        private String type;

        @Schema(description = "服务持续时长", example = "12")
        private Integer duration;

        @Schema(description = "服务周期描述", example = "按月服务，共12个月")
        private String description;
    }
}
