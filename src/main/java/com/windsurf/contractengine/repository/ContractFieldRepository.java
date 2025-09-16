package com.windsurf.contractengine.repository;

import com.windsurf.contractengine.entity.ContractField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 合同字段数据访问接口
 */
@Repository
public interface ContractFieldRepository extends JpaRepository<ContractField, Long> {

    /**
     * 根据合同ID查找所有字段
     */
    List<ContractField> findByContractIdOrderByFieldName(Long contractId);

    /**
     * 根据合同ID和字段名查找字段
     */
    Optional<ContractField> findByContractIdAndFieldName(Long contractId, String fieldName);

    /**
     * 根据合同ID删除所有字段
     */
    void deleteByContractId(Long contractId);

    /**
     * 查找置信度高于阈值的字段
     */
    @Query("SELECT cf FROM ContractField cf WHERE cf.contract.id = :contractId AND cf.confidenceScore >= :threshold")
    List<ContractField> findHighConfidenceFields(@Param("contractId") Long contractId, 
                                                 @Param("threshold") Double threshold);

    /**
     * 统计合同的字段提取数量
     */
    @Query("SELECT COUNT(cf) FROM ContractField cf WHERE cf.contract.id = :contractId")
    Long countByContractId(@Param("contractId") Long contractId);

    /**
     * 根据字段类型查找字段
     */
    List<ContractField> findByContractIdAndFieldType(Long contractId, String fieldType);
}
