package com.windsurf.contractengine.repository;

import com.windsurf.contractengine.entity.PaymentSchedule;
import com.windsurf.contractengine.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 支付计划数据访问层
 */
@Repository
public interface PaymentScheduleRepository extends JpaRepository<PaymentSchedule, Long> {

    /**
     * 根据合同ID查询支付计划列表
     * 
     * @param contractId 合同ID
     * @return 支付计划列表
     */
    @Query("SELECT p FROM PaymentSchedule p WHERE p.contract.id = :contractId ORDER BY p.periodNumber ASC")
    List<PaymentSchedule> findByContractIdOrderByPeriodNumber(@Param("contractId") Long contractId);

    /**
     * 根据合同ID和状态查询支付计划列表
     * 
     * @param contractId 合同ID
     * @param status 支付状态
     * @return 支付计划列表
     */
    @Query("SELECT p FROM PaymentSchedule p WHERE p.contract.id = :contractId AND p.status = :status ORDER BY p.periodNumber ASC")
    List<PaymentSchedule> findByContractIdAndStatusOrderByPeriodNumber(@Param("contractId") Long contractId, @Param("status") PaymentStatus status);

    /**
     * 根据合同ID查询已完成的支付记录
     * 
     * @param contractId 合同ID
     * @return 已完成的支付记录列表
     */
    @Query("SELECT p FROM PaymentSchedule p WHERE p.contract.id = :contractId AND p.status = 'PAID' AND p.actualDate IS NOT NULL ORDER BY p.actualDate ASC")
    List<PaymentSchedule> findCompletedPaymentsByContractId(@Param("contractId") Long contractId);

    /**
     * 根据合同ID查询实际付款记录（有实际付款日期和金额的记录）
     * 
     * @param contractId 合同ID
     * @return 实际付款记录列表
     */
    @Query("SELECT p FROM PaymentSchedule p WHERE p.contract.id = :contractId AND p.actualDate IS NOT NULL AND p.actualAmount IS NOT NULL ORDER BY p.actualDate ASC")
    List<PaymentSchedule> findActualPaymentsByContractId(@Param("contractId") Long contractId);

    /**
     * 根据合同ID和日期范围查询支付记录
     * 
     * @param contractId 合同ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 支付记录列表
     */
    @Query("SELECT p FROM PaymentSchedule p WHERE p.contract.id = :contractId AND p.actualDate BETWEEN :startDate AND :endDate ORDER BY p.actualDate ASC")
    List<PaymentSchedule> findByContractIdAndDateRange(@Param("contractId") Long contractId, 
                                                       @Param("startDate") LocalDate startDate, 
                                                       @Param("endDate") LocalDate endDate);

    /**
     * 检查合同是否存在支付记录
     * 
     * @param contractId 合同ID
     * @return 是否存在支付记录
     */
    @Query("SELECT COUNT(p) > 0 FROM PaymentSchedule p WHERE p.contract.id = :contractId")
    boolean existsByContractId(@Param("contractId") Long contractId);

    /**
     * 根据合同ID删除所有支付计划
     * 
     * @param contractId 合同ID
     */
    @Query("DELETE FROM PaymentSchedule p WHERE p.contract.id = :contractId")
    void deleteByContractId(@Param("contractId") Long contractId);
}
