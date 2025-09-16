package com.windsurf.contractengine.repository;

import com.windsurf.contractengine.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 合同数据访问接口
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    /**
     * 根据合同编号查找合同
     */
    Optional<Contract> findByContractNumber(String contractNumber);

    /**
     * 检查合同编号是否存在
     */
    boolean existsByContractNumber(String contractNumber);

    /**
     * 分页查询合同，支持多条件过滤
     */
    @Query("SELECT c FROM Contract c WHERE " +
           "(:contractType IS NULL OR c.contractType = :contractType) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:keyword IS NULL OR " +
           " c.contractName LIKE %:keyword% OR " +
           " c.contractNumber LIKE %:keyword% OR " +
           " c.counterparty LIKE %:keyword%)")
    Page<Contract> findContractsWithFilters(@Param("contractType") String contractType,
                                           @Param("status") Contract.ContractStatus status,
                                           @Param("keyword") String keyword,
                                           Pageable pageable);

    /**
     * 根据合同类型统计数量
     */
    @Query("SELECT c.contractType, COUNT(c) FROM Contract c GROUP BY c.contractType")
    Object[] countByContractType();

    /**
     * 根据状态统计数量
     */
    @Query("SELECT c.status, COUNT(c) FROM Contract c GROUP BY c.status")
    Object[] countByStatus();

    /**
     * 查找即将到期的合同（30天内）
     */
    @Query("SELECT c FROM Contract c WHERE c.endDate <= CURRENT_DATE + 30 AND c.status = 'ACTIVE'")
    Page<Contract> findExpiringContracts(Pageable pageable);

    /**
     * 根据对方公司查找合同
     */
    Page<Contract> findByCounterpartyContainingIgnoreCase(String counterparty, Pageable pageable);
}
