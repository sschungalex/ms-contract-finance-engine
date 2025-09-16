package com.windsurf.contractengine.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 合同实体类
 */
@Entity
@Table(name = "contracts")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 合同编号
     */
    @Column(name = "contract_number", unique = true, nullable = false, length = 100)
    private String contractNumber;

    /**
     * 合同名称
     */
    @Column(name = "contract_name", nullable = false, length = 200)
    private String contractName;

    /**
     * 合同类型
     */
    @Column(name = "contract_type", nullable = false, length = 50)
    private String contractType;

    /**
     * 合同对方
     */
    @Column(name = "counterparty", nullable = false, length = 200)
    private String counterparty;

    /**
     * 合同开始日期
     */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * 合同结束日期
     */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * 合同状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ContractStatus status = ContractStatus.DRAFT;

    /**
     * 合同总金额
     */
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    /**
     * 币种
     */
    @Column(name = "currency", length = 10)
    private String currency = "CNY";

    /**
     * 支付频率
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_frequency", nullable = false)
    private PaymentFrequency paymentFrequency;

    /**
     * 支付方式
     */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    /**
     * 原始文件名
     */
    @Column(name = "original_filename")
    private String originalFilename;

    /**
     * 文件存储路径
     */
    @Column(name = "file_path", length = 500)
    private String filePath;

    /**
     * 文件大小(字节)
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 提取的合同文本
     */
    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * 提取的字段
     */
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContractField> extractedFields = new ArrayList<>();

    /**
     * 支付计划
     */
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentSchedule> paymentSchedules = new ArrayList<>();

    /**
     * 摊销计划
     */
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AmortizationSchedule> amortizationSchedules = new ArrayList<>();

    /**
     * 会计分录
     */
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JournalEntry> journalEntries = new ArrayList<>();

    /**
     * 合同状态枚举
     */
    public enum ContractStatus {
        DRAFT("草稿"),
        ACTIVE("生效"),
        EXPIRED("过期"),
        TERMINATED("终止");

        private final String description;

        ContractStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 支付频率枚举
     */
    public enum PaymentFrequency {
        MONTHLY("月付"),
        QUARTERLY("季付"),
        SEMI_ANNUALLY("半年付"),
        ANNUALLY("年付"),
        ONE_TIME("一次性");

        private final String description;

        PaymentFrequency(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
