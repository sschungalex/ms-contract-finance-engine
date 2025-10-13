package com.windsurf.contractengine.dto;

import com.windsurf.contractengine.enums.ContractStatus;
import com.windsurf.contractengine.enums.PaymentFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 合同更新请求DTO - API 1.4 数据修正编辑
 */
@Data
@Schema(description = "合同更新请求")
public class ContractUpdateRequest {

    // ========== 基本字段（旧接口兼容） ==========
    @Size(max = 200, message = "合同名称长度不能超过200字符")
    @Schema(description = "合同名称", example = "软件开发服务合同")
    private String contractName;

    @Size(max = 50, message = "合同类型长度不能超过50字符")
    @Schema(description = "合同类型", example = "服务合同")
    private String contractType;

    @Size(max = 200, message = "合同对方长度不能超过200字符")
    @Schema(description = "合同对方", example = "ABC科技有限公司")
    private String counterparty;

    @Schema(description = "合同开始日期", example = "2024-01-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "合同结束日期", example = "2024-12-31T23:59:59")
    private LocalDateTime endDate;

    @Schema(description = "合同状态", example = "ACTIVE")
    private ContractStatus status;

    @Size(max = 10, message = "币种长度不能超过10字符")
    @Schema(description = "币种", example = "CNY")
    private String currency;

    @Schema(description = "支付频率", example = "MONTHLY")
    private PaymentFrequency paymentFrequency;

    // ========== API 1.4 规范字段 ==========
    // 注意：为支持部分更新，这些字段不使用@NotNull，由业务层决定是否必填
    @Size(max = 50, message = "支付方式长度不能超过50字符")
    @Schema(description = "支付方式", example = "银行转账")
    private String paymentMethod;

    @DecimalMin(value = "0.01", message = "合同总金额必须大于0")
    @Digits(integer = 12, fraction = 2, message = "金额格式不正确，最多12位整数2位小数")
    @Schema(description = "合同总金额", example = "100000.00")
    private BigDecimal totalAmount;

    @Size(max = 10, message = "付款日期最多10个")
    @Schema(description = "付款日期列表", example = "[\"2025-01-01\", \"2025-03-01\"]")
    private List<LocalDate> paymentDates;

    @DecimalMin(value = "0", message = "税率不能小于0")
    @DecimalMax(value = "100", message = "税率不能大于100")
    @Schema(description = "税率", example = "6.00")
    private BigDecimal taxRate;

    @Size(max = 500, message = "备注长度不能超过500字符")
    @Schema(description = "备注", example = "备注信息")
    private String remarks;

    @Valid
    @Schema(description = "金额要素")
    private AmountElements amountElements;

    @Valid
    @Schema(description = "时间要素")
    private TimeElements timeElements;

    @Schema(description = "合同签订日期", example = "2025-01-01")
    private LocalDate contractDate;

    @Size(max = 10, message = "合同当事方最多10个")
    @Schema(description = "合同当事方", example = "[\"甲方公司\", \"乙方公司\"]")
    private List<String> parties;

    /**
     * 金额要素内部类
     */
    @Data
    @Schema(description = "金额要素")
    public static class AmountElements {
        @DecimalMin(value = "0.01", message = "合同总金额必须大于0")
        @Schema(description = "合同总金额", example = "100000.00")
        private BigDecimal totalAmount;

        @DecimalMin(value = "0.01", message = "单价必须大于0")
        @Schema(description = "单价", example = "5000.00")
        private BigDecimal unitPrice;

        @Min(value = 1, message = "数量必须大于0")
        @Schema(description = "数量", example = "20")
        private Integer quantity;
    }

    /**
     * 时间要素内部类
     */
    @Data
    @Schema(description = "时间要素")
    public static class TimeElements {
        @Valid
        @Schema(description = "服务周期")
        private ServicePeriod servicePeriod;

        @Valid
        @Size(max = 20, message = "交付节点最多20个")
        @Schema(description = "交付节点列表")
        private List<DeliveryNode> deliveryNodes;
    }

    /**
     * 服务周期内部类
     */
    @Data
    @Schema(description = "服务周期")
    public static class ServicePeriod {
        @Pattern(regexp = "ONCE|WEEKLY|MONTHLY|QUARTERLY|YEARLY", 
                message = "服务周期类型必须是: ONCE/WEEKLY/MONTHLY/QUARTERLY/YEARLY")
        @Schema(description = "服务周期类型", example = "MONTHLY")
        private String type;

        @Min(value = 1, message = "持续时长必须大于0")
        @Schema(description = "持续时长", example = "12")
        private Integer duration;

        @Size(max = 200, message = "描述信息长度不能超过200字符")
        @Schema(description = "描述信息", example = "按月服务，共12个月")
        private String description;
    }

    /**
     * 交付节点内部类
     */
    @Data
    @Schema(description = "交付节点")
    public static class DeliveryNode {
        @Size(max = 100, message = "里程碑名称长度不能超过100字符")
        @Schema(description = "里程碑名称", example = "项目启动")
        private String milestone;

        @Min(value = 0, message = "交付百分比不能小于0")
        @Max(value = 100, message = "交付百分比不能大于100")
        @Schema(description = "交付百分比", example = "30")
        private Integer percentage;

        @Schema(description = "预期交付日期", example = "2025-01-15")
        private LocalDate dueDate;
    }
}
