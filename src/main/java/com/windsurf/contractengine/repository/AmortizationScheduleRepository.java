package com.windsurf.contractengine.repository;

import com.windsurf.contractengine.entity.AmortizationSchedule;
import com.windsurf.contractengine.enums.AmortizationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 摊销计划数据访问层
 */
@Repository
public interface AmortizationScheduleRepository extends JpaRepository<AmortizationSchedule, Long> {

    /**
     * 根据合同ID查询摊销计划列表
     * 
     * @param contractId 合同ID
     * @return 摊销计划列表
     */
    @Query("SELECT a FROM AmortizationSchedule a WHERE a.contract.id = :contractId ORDER BY a.periodNumber ASC")
    List<AmortizationSchedule> findByContractIdOrderByPeriodNumber(@Param("contractId") Long contractId);

    /**
     * 根据合同ID和状态查询摊销计划列表
     * 
     * @param contractId 合同ID
     * @param status 摊销状态
     * @return 摊销计划列表
     */
    @Query("SELECT a FROM AmortizationSchedule a WHERE a.contract.id = :contractId AND a.status = :status ORDER BY a.periodNumber ASC")
    List<AmortizationSchedule> findByContractIdAndStatusOrderByPeriodNumber(@Param("contractId") Long contractId, @Param("status") AmortizationStatus status);

    /**
     * 根据合同ID查询未完成的摊销计划列表
     * 
     * @param contractId 合同ID
     * @return 未完成的摊销计划列表
     */
    @Query("SELECT a FROM AmortizationSchedule a WHERE a.contract.id = :contractId AND a.status IN ('PENDING', 'IN_PROGRESS') ORDER BY a.periodNumber ASC")
    List<AmortizationSchedule> findPendingByContractId(@Param("contractId") Long contractId);

    /**
     * 检查合同是否存在摊销记录
     * 
     * @param contractId 合同ID
     * @return 是否存在摊销记录
     */
    @Query("SELECT COUNT(a) > 0 FROM AmortizationSchedule a WHERE a.contract.id = :contractId")
    boolean existsByContractId(@Param("contractId") Long contractId);

    /**
     * 根据合同ID删除所有摊销计划
     * 
     * @param contractId 合同ID
     */
    @Query("DELETE FROM AmortizationSchedule a WHERE a.contract.id = :contractId")
    void deleteByContractId(@Param("contractId") Long contractId);
}
