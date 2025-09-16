package com.windsurf.contractengine.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会计分录明细实体类
 */
@Entity
@Table(name = "journal_entry_lines")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class JournalEntryLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的会计分录
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    /**
     * 行号
     */
    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    /**
     * 科目代码
     */
    @Column(name = "account_code", nullable = false, length = 50)
    private String accountCode;

    /**
     * 科目名称
     */
    @Column(name = "account_name", nullable = false, length = 200)
    private String accountName;

    /**
     * 借方金额
     */
    @Column(name = "debit_amount", precision = 15, scale = 2)
    private BigDecimal debitAmount = BigDecimal.ZERO;

    /**
     * 贷方金额
     */
    @Column(name = "credit_amount", precision = 15, scale = 2)
    private BigDecimal creditAmount = BigDecimal.ZERO;

    /**
     * 摘要
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 辅助核算信息（JSON格式）
     */
    @Column(name = "auxiliary_info", columnDefinition = "TEXT")
    private String auxiliaryInfo;

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
