package com.windsurf.contractengine.dto;

import com.windsurf.contractengine.enums.AIProcessingStatus;
import com.windsurf.contractengine.enums.ContractStatus;
import com.windsurf.contractengine.enums.PaymentFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 合同响应DTO
 * 包含合同的完整信息，包括AI提取的结构化数据
 */
@Data
@Schema(description = "合同响应")
public class ContractResponse {

    @Schema(description = "合同ID", example = "123")
    private Long id;

    @Schema(description = "合同编号", example = "CT202401001")
    private String contractNumber;

    @Schema(description = "合同名称", example = "软件开发服务合同")
    private String contractName;

    @Schema(description = "合同类型", example = "服务合同")
    private String contractType;

    @Schema(description = "合同对方", example = "ABC科技有限公司")
    private String counterparty;

    @Schema(description = "合同状态", example = "ACTIVE")
    private ContractStatus status;

    @Schema(description = "AI处理状态", example = "COMPLETED")
    private AIProcessingStatus aiProcessingStatus;

    @Schema(description = "AI置信度", example = "0.95")
    private BigDecimal aiConfidence;

    @Schema(description = "原始文件名", example = "contract_abc.pdf")
    private String originalFilename;

    @Schema(description = "文件大小", example = "1024000")
    private Long fileSize;

    @Schema(description = "上传时间", example = "2025-01-01T10:00:00Z")
    private LocalDateTime uploadTime;

    @Schema(description = "支付方式", example = "银行转账")
    private String paymentMethod;

    @Schema(description = "合同总金额", example = "100000.00")
    private BigDecimal totalAmount;

    @Schema(description = "币种", example = "CNY")
    private String currency;

    @Schema(description = "支付频率", example = "MONTHLY")
    private PaymentFrequency paymentFrequency;

    @Schema(description = "付款日期列表", example = "[\"2025-01-01\", \"2025-03-01\"]")
    private List<LocalDate> paymentDates;

    @Schema(description = "税率", example = "6.00")
    private BigDecimal taxRate;

    @Schema(description = "备注", example = "备注信息")
    private String remarks;

    @Schema(description = "合同签订日期", example = "2025-01-01")
    private LocalDate contractDate;

    @Schema(description = "合同当事方", example = "[\"甲方公司\", \"乙方公司\"]")
    private List<String> parties;

    @Schema(description = "金额要素（JSON结构）")
    private AmountElements amountElements;

    @Schema(description = "时间要素（JSON结构）")
    private TimeElements timeElements;

    @Schema(description = "单价", example = "5000.00")
    private BigDecimal unitPrice;

    @Schema(description = "数量", example = "20")
    private Integer quantity;

    @Schema(description = "服务周期类型", example = "MONTHLY")
    private String servicePeriodType;

    @Schema(description = "服务持续时长", example = "12")
    private Integer serviceDuration;

    @Schema(description = "服务描述", example = "按月服务，共12个月")
    private String serviceDescription;

    @Schema(description = "合同开始日期", example = "2025-01-01")
    private LocalDate startDate;

    @Schema(description = "合同结束日期", example = "2025-12-31")
    private LocalDate endDate;

    @Schema(description = "创建时间", example = "2025-01-01T10:00:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2025-01-01T10:30:00Z")
    private LocalDateTime updatedAt;

    @Schema(description = "创建人", example = "张三")
    private String createdBy;

    /**
     * 金额要素内部类
     */
    @Data
    @Schema(description = "金额要素")
    public static class AmountElements {
        @Schema(description = "合同总金额", example = "100000.00")
        private BigDecimal totalAmount;

        @Schema(description = "单价", example = "5000.00")
        private BigDecimal unitPrice;

        @Schema(description = "数量", example = "20")
        private Integer quantity;
    }

    /**
     * 时间要素内部类
     */
    @Data
    @Schema(description = "时间要素")
    public static class TimeElements {
        @Schema(description = "服务周期")
        private ServicePeriod servicePeriod;

        @Schema(description = "交付节点列表")
        private List<DeliveryNode> deliveryNodes;
    }

    /**
     * 服务周期内部类
     */
    @Data
    @Schema(description = "服务周期")
    public static class ServicePeriod {
        @Schema(description = "服务周期类型", example = "MONTHLY")
        private String type;

        @Schema(description = "持续时长", example = "12")
        private Integer duration;

        @Schema(description = "描述信息", example = "按月服务，共12个月")
        private String description;
    }

    /**
     * 交付节点内部类
     */
    @Data
    @Schema(description = "交付节点")
    public static class DeliveryNode {
        @Schema(description = "里程碑名称", example = "项目启动")
        private String milestone;

        @Schema(description = "交付百分比", example = "30")
        private Integer percentage;

        @Schema(description = "预期交付日期", example = "2025-01-15")
        private LocalDate dueDate;
    }
}
