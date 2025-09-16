package com.windsurf.contractengine.service;

import com.windsurf.contractengine.entity.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 会计分录服务接口
 */
public interface AccountingService {

    /**
     * 根据合同生成会计分录
     * 
     * @param contract 合同实体
     * @return 生成的会计分录列表
     */
    List<JournalEntry> generateJournalEntries(Contract contract);

    /**
     * 根据支付计划生成支付分录
     * 
     * @param paymentSchedule 支付计划
     * @return 支付分录
     */
    JournalEntry generatePaymentEntry(PaymentSchedule paymentSchedule);

    /**
     * 根据摊销计划生成摊销分录
     * 
     * @param amortizationSchedule 摊销计划
     * @return 摊销分录
     */
    JournalEntry generateAmortizationEntry(AmortizationSchedule amortizationSchedule);

    /**
     * 生成差额分录（多付/少付）
     * 
     * @param paymentSchedule 支付计划
     * @param varianceAmount 差额金额
     * @return 差额分录
     */
    JournalEntry generateVarianceEntry(PaymentSchedule paymentSchedule, BigDecimal varianceAmount);

    /**
     * 验证分录借贷平衡
     * 
     * @param journalEntry 会计分录
     * @return 是否平衡
     */
    boolean validateJournalEntryBalance(JournalEntry journalEntry);

    /**
     * 过账分录
     * 
     * @param journalEntryId 分录ID
     */
    void postJournalEntry(Long journalEntryId);

    /**
     * 取消分录
     * 
     * @param journalEntryId 分录ID
     */
    void cancelJournalEntry(Long journalEntryId);

    /**
     * 获取分录规则配置
     * 
     * @param contractType 合同类型
     * @param entryType 分录类型
     * @return 分录规则配置
     */
    Map<String, Object> getAccountingRules(String contractType, JournalEntry.EntryType entryType);

    /**
     * 生成分录编号
     * 
     * @param entryType 分录类型
     * @return 分录编号
     */
    String generateEntryNumber(JournalEntry.EntryType entryType);

    /**
     * 创建分录明细行
     * 
     * @param accountCode 科目代码
     * @param accountName 科目名称
     * @param debitAmount 借方金额
     * @param creditAmount 贷方金额
     * @param description 摘要
     * @return 分录明细行
     */
    JournalEntryLine createEntryLine(String accountCode, String accountName, 
                                   BigDecimal debitAmount, BigDecimal creditAmount, 
                                   String description);

    /**
     * 获取科目配置
     * 
     * @param accountType 科目类型
     * @return 科目配置信息
     */
    Map<String, String> getAccountConfiguration(String accountType);

    /**
     * 批量生成分录
     * 
     * @param contracts 合同列表
     * @return 生成的分录数量
     */
    int batchGenerateJournalEntries(List<Contract> contracts);

    /**
     * 生成调整分录
     * 
     * @param originalEntry 原分录
     * @param adjustmentAmount 调整金额
     * @param reason 调整原因
     * @return 调整分录
     */
    JournalEntry generateAdjustmentEntry(JournalEntry originalEntry, 
                                       BigDecimal adjustmentAmount, 
                                       String reason);
}
