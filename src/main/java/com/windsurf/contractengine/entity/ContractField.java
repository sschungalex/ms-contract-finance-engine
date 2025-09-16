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
 * 合同字段提取结果实体类
 */
@Entity
@Table(name = "contract_fields")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class ContractField {

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
     * 字段名称
     */
    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    /**
     * 字段值
     */
    @Column(name = "field_value", columnDefinition = "TEXT")
    private String fieldValue;

    /**
     * 字段类型
     */
    @Column(name = "field_type", length = 50)
    private String fieldType;

    /**
     * 置信度分数
     */
    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    /**
     * 提取方法
     */
    @Column(name = "extraction_method", length = 50)
    private String extractionMethod;

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
