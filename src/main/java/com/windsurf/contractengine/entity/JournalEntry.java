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
import java.util.ArrayList;
import java.util.List;

/**
 * 会计分录实体类
 */
@Entity
@Table(name = "journal_entries")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class JournalEntry {

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
     * 分录编号
     */
    @Column(name = "entry_number", unique = true, nullable = false, length = 100)
    private String entryNumber;

    /**
     * 分录日期
     */
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    /**
     * 分录类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private EntryType entryType;

    /**
     * 分录描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 总金额
     */
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    /**
     * 分录状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EntryStatus status = EntryStatus.DRAFT;

    /**
     * 关联的支付计划ID
     */
    @Column(name = "payment_schedule_id")
    private Long paymentScheduleId;

    /**
     * 关联的摊销计划ID
     */
    @Column(name = "amortization_schedule_id")
    private Long amortizationScheduleId;

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
     * 创建人
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * 分录明细
     */
    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JournalEntryLine> entryLines = new ArrayList<>();

    /**
     * 分录类型枚举
     */
    public enum EntryType {
        PAYMENT("支付分录"),
        AMORTIZATION("摊销分录"),
        VARIANCE("差额分录"),
        ADJUSTMENT("调整分录");

        private final String description;

        EntryType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 分录状态枚举
     */
    public enum EntryStatus {
        DRAFT("草稿"),
        POSTED("已过账"),
        CANCELLED("已取消");

        private final String description;

        EntryStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
