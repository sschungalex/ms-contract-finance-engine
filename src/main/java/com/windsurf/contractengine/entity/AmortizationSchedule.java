package com.windsurf.contractengine.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 摊销计划实体类
 */
@Entity
@Table(name = "amortization_schedules")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class AmortizationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的合同
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    /**
     * 期数
     */
    @Column(name = "period_number", nullable = false)
    private Integer periodNumber;

    /**
     * 摊销日期
     */
    @Column(name = "amortization_date", nullable = false)
    private LocalDate amortizationDate;

    /**
     * 摊销金额
     */
    @Column(name = "amortization_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amortizationAmount;

    /**
     * 累计摊销金额
     */
    @Column(name = "accumulated_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal accumulatedAmount;

    /**
     * 剩余金额
     */
    @Column(name = "remaining_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingAmount;

    /**
     * 摊销状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AmortizationStatus status = AmortizationStatus.PENDING;

    /**
     * 备注
     */
    @Column(name = "remarks", length = 500)
    private String remarks;

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
     * 摊销状态枚举
     */
    public enum AmortizationStatus {
        PENDING("待摊销"),
        PROCESSED("已摊销"),
        CANCELLED("已取消");

        private final String description;

        AmortizationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
