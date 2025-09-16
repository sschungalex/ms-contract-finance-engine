package com.windsurf.contractengine.service;

import com.windsurf.contractengine.entity.AmortizationSchedule;
import com.windsurf.contractengine.entity.Contract;
import com.windsurf.contractengine.entity.PaymentSchedule;

import java.util.List;

/**
 * 计划生成服务接口
 */
public interface ScheduleGenerationService {

    /**
     * 生成支付计划
     * 
     * @param contract 合同实体
     * @return 支付计划列表
     */
    List<PaymentSchedule> generatePaymentSchedule(Contract contract);

    /**
     * 生成摊销计划
     * 
     * @param contract 合同实体
     * @return 摊销计划列表
     */
    List<AmortizationSchedule> generateAmortizationSchedule(Contract contract);

    /**
     * 更新支付计划状态
     * 
     * @param scheduleId 计划ID
     * @param actualAmount 实际支付金额
     * @param actualDate 实际支付日期
     */
    void updatePaymentStatus(Long scheduleId, java.math.BigDecimal actualAmount, 
                           java.time.LocalDate actualDate);

    /**
     * 计算支付差额
     * 
     * @param schedule 支付计划
     * @return 差额金额
     */
    java.math.BigDecimal calculatePaymentVariance(PaymentSchedule schedule);

    /**
     * 验证计划的完整性
     * 
     * @param contract 合同实体
     * @param paymentSchedules 支付计划列表
     * @return 验证结果
     */
    boolean validateScheduleIntegrity(Contract contract, List<PaymentSchedule> paymentSchedules);

    /**
     * 根据支付频率计算期数
     * 
     * @param contract 合同实体
     * @return 总期数
     */
    int calculateTotalPeriods(Contract contract);

    /**
     * 计算每期支付金额
     * 
     * @param contract 合同实体
     * @param totalPeriods 总期数
     * @return 每期金额
     */
    java.math.BigDecimal calculatePerPeriodAmount(Contract contract, int totalPeriods);

    /**
     * 生成跨期摊销计划
     * 
     * @param contract 合同实体
     * @param startDate 摊销开始日期
     * @param endDate 摊销结束日期
     * @return 摊销计划列表
     */
    List<AmortizationSchedule> generateCrossPeriodAmortization(Contract contract, 
                                                              java.time.LocalDate startDate,
                                                              java.time.LocalDate endDate);
}
