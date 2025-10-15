package com.windsurf.contractengine.repository;

import com.windsurf.contractengine.entity.JournalEntry;
import com.windsurf.contractengine.enums.EntryStatus;
import com.windsurf.contractengine.enums.EntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 会计分录数据访问层
 */
@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    /**
     * 根据合同ID查询会计分录列表
     * 
     * @param contractId 合同ID
     * @return 会计分录列表
     */
    @Query("SELECT j FROM JournalEntry j LEFT JOIN FETCH j.entryLines WHERE j.contract.id = :contractId ORDER BY j.entryDate ASC, j.id ASC")
    List<JournalEntry> findByContractIdWithLines(@Param("contractId") Long contractId);

    /**
     * 根据合同ID和分录类型查询会计分录列表
     * 
     * @param contractId 合同ID
     * @param entryType 分录类型
     * @return 会计分录列表
     */
    @Query("SELECT j FROM JournalEntry j LEFT JOIN FETCH j.entryLines WHERE j.contract.id = :contractId AND j.entryType = :entryType ORDER BY j.entryDate ASC")
    List<JournalEntry> findByContractIdAndEntryTypeWithLines(@Param("contractId") Long contractId, @Param("entryType") EntryType entryType);

    /**
     * 根据合同ID和状态查询会计分录列表
     * 
     * @param contractId 合同ID
     * @param status 分录状态
     * @return 会计分录列表
     */
    @Query("SELECT j FROM JournalEntry j LEFT JOIN FETCH j.entryLines WHERE j.contract.id = :contractId AND j.status = :status ORDER BY j.entryDate ASC")
    List<JournalEntry> findByContractIdAndStatusWithLines(@Param("contractId") Long contractId, @Param("status") EntryStatus status);

    /**
     * 根据分录ID查询会计分录
     * 
     * @param entryId 分录ID
     * @return 会计分录
     */
    @Query("SELECT j FROM JournalEntry j LEFT JOIN FETCH j.entryLines WHERE j.entryId = :entryId")
    Optional<JournalEntry> findByEntryIdWithLines(@Param("entryId") String entryId);

    /**
     * 根据合同ID和日期范围查询会计分录列表
     * 
     * @param contractId 合同ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 会计分录列表
     */
    @Query("SELECT j FROM JournalEntry j LEFT JOIN FETCH j.entryLines WHERE j.contract.id = :contractId AND j.entryDate BETWEEN :startDate AND :endDate ORDER BY j.entryDate ASC")
    List<JournalEntry> findByContractIdAndDateRangeWithLines(@Param("contractId") Long contractId, 
                                                             @Param("startDate") LocalDate startDate, 
                                                             @Param("endDate") LocalDate endDate);

    /**
     * 检查合同是否存在会计分录
     * 
     * @param contractId 合同ID
     * @return 是否存在会计分录
     */
    @Query("SELECT COUNT(j) > 0 FROM JournalEntry j WHERE j.contract.id = :contractId")
    boolean existsByContractId(@Param("contractId") Long contractId);

    /**
     * 根据合同ID删除所有会计分录
     * 
     * @param contractId 合同ID
     */
    @Query("DELETE FROM JournalEntry j WHERE j.contract.id = :contractId")
    void deleteByContractId(@Param("contractId") Long contractId);

    /**
     * 根据参考号查询会计分录
     * 
     * @param reference 参考号
     * @return 会计分录列表
     */
    @Query("SELECT j FROM JournalEntry j LEFT JOIN FETCH j.entryLines WHERE j.reference = :reference")
    List<JournalEntry> findByReferenceWithLines(@Param("reference") String reference);
}
