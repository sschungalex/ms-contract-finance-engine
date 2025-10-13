package com.windsurf.contractengine.entity;

import com.windsurf.contractengine.enums.PaymentStatus;
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
 * 支付计划实体类
 */
@Entity
@Table(name = "payment_schedules")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class PaymentSchedule {

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
     * 计划支付日期
     */
    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    /**
     * 计划支付金额
     */
    @Column(name = "scheduled_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal scheduledAmount;

    /**
     * 实际支付日期
     */
    @Column(name = "actual_date")
    private LocalDate actualDate;

    /**
     * 实际支付金额
     */
    @Column(name = "actual_amount", precision = 15, scale = 2)
    private BigDecimal actualAmount;

    /**
     * 支付方式
     */
    @Column(name = "payment_method", length = 100)
    private String paymentMethod;

    /**
     * 支付描述
     */
    @Column(name = "payment_description", length = 500)
    private String paymentDescription;

    /**
     * 支付状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * 差额（实际-计划）
     */
    @Column(name = "variance_amount", precision = 15, scale = 2)
    private BigDecimal varianceAmount;

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
}
