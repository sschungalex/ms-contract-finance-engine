package com.windsurf.contractengine.service;

import com.windsurf.contractengine.dto.JournalEntryGenerationRequest;
import com.windsurf.contractengine.dto.JournalEntryGenerationResponse;
import com.windsurf.contractengine.entity.AmortizationSchedule;
import com.windsurf.contractengine.entity.Contract;
import com.windsurf.contractengine.enums.AmortizationStatus;
import com.windsurf.contractengine.repository.AmortizationScheduleRepository;
import com.windsurf.contractengine.repository.ContractRepository;
import com.windsurf.contractengine.repository.JournalEntryRepository;
import com.windsurf.contractengine.repository.PaymentScheduleRepository;
import com.windsurf.contractengine.service.impl.ContractServiceImpl;
import com.windsurf.contractengine.util.ContractUpdateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 会计分录生成测试类
 */
@ExtendWith(MockitoExtension.class)
class JournalEntryGenerationTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private AmortizationScheduleRepository amortizationScheduleRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private PaymentScheduleRepository paymentScheduleRepository;

    @Mock
    private ContractUpdateUtil contractUpdateUtil;

    @InjectMocks
    private ContractServiceImpl contractService;

    private Contract testContract;
    private JournalEntryGenerationRequest testRequest;

    @BeforeEach
    void setUp() {
        // 创建测试合同
        testContract = new Contract();
        testContract.setId(123L);
        testContract.setContractNumber("TEST-001");
        testContract.setTotalAmount(new BigDecimal("120000.00"));

        // 创建测试请求
        testRequest = new JournalEntryGenerationRequest();
        
        JournalEntryGenerationRequest.ContractInfo contractInfo = new JournalEntryGenerationRequest.ContractInfo();
        contractInfo.setContractId(123L);
        contractInfo.setTotalAmount(new BigDecimal("120000.00"));
        
        JournalEntryGenerationRequest.PaymentPeriod paymentPeriod = new JournalEntryGenerationRequest.PaymentPeriod();
        paymentPeriod.setType("MONTHLY");
        paymentPeriod.setDuration(12);
        paymentPeriod.setDescription("按月服务，共12个月");
        contractInfo.setPaymentPeriod(paymentPeriod);
        
        testRequest.setContractInfo(contractInfo);
    }

    @Test
    void testGenerateJournalEntriesWithActualPayments() {
        // 准备测试数据
        JournalEntryGenerationRequest.ActualPayment payment = new JournalEntryGenerationRequest.ActualPayment();
        payment.setPaymentDate(LocalDate.of(2025, 1, 15));
        payment.setAmount(new BigDecimal("30000.00"));
        payment.setPaymentMethod("银行转账");
        payment.setDescription("首期付款");
        
        testRequest.setActualPayments(Collections.singletonList(payment));

        // Mock 行为
        when(contractRepository.findById(123L)).thenReturn(Optional.of(testContract));
        when(amortizationScheduleRepository.existsByContractId(123L)).thenReturn(false);

        // 执行测试
        JournalEntryGenerationResponse response = contractService.generateJournalEntries(123L, testRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(123L, response.getContractId());
        assertNotNull(response.getJournalEntries());
        assertTrue(response.getJournalEntries().size() > 0);
        
        // 验证付款分录
        boolean hasPaymentEntry = response.getJournalEntries().stream()
                .anyMatch(entry -> entry.getDescription().contains("预付款"));
        assertTrue(hasPaymentEntry, "应该包含付款确认分录");

        // 验证摊销分录
        boolean hasAmortizationEntry = response.getJournalEntries().stream()
                .anyMatch(entry -> entry.getDescription().contains("摊销"));
        assertTrue(hasAmortizationEntry, "应该包含摊销分录");
    }

    @Test
    void testGenerateJournalEntriesWithExistingAmortizationSchedule() {
        // 准备摊销记录
        AmortizationSchedule schedule1 = new AmortizationSchedule();
        schedule1.setId(1L);
        schedule1.setPeriodNumber(1);
        schedule1.setPeriod("2025-01");
        schedule1.setAmortizationDate(LocalDate.of(2025, 1, 31));
        schedule1.setAmortizationAmount(new BigDecimal("10000.00"));
        schedule1.setStatus(AmortizationStatus.PENDING);

        AmortizationSchedule schedule2 = new AmortizationSchedule();
        schedule2.setId(2L);
        schedule2.setPeriodNumber(2);
        schedule2.setPeriod("2025-02");
        schedule2.setAmortizationDate(LocalDate.of(2025, 2, 28));
        schedule2.setAmortizationAmount(new BigDecimal("10000.00"));
        schedule2.setStatus(AmortizationStatus.PENDING);

        // 不设置实际付款信息，测试使用摊销记录生成分录
        testRequest.setActualPayments(null);

        // Mock 行为
        when(contractRepository.findById(123L)).thenReturn(Optional.of(testContract));
        when(amortizationScheduleRepository.existsByContractId(123L)).thenReturn(true);
        when(amortizationScheduleRepository.findByContractIdOrderByPeriodNumber(123L))
                .thenReturn(Arrays.asList(schedule1, schedule2));

        // 执行测试
        JournalEntryGenerationResponse response = contractService.generateJournalEntries(123L, testRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(123L, response.getContractId());
        assertNotNull(response.getJournalEntries());
        
        // 验证基于摊销记录生成的分录
        boolean hasScheduleBasedEntry = response.getJournalEntries().stream()
                .anyMatch(entry -> entry.getReference().contains("Amortization-Schedule"));
        assertTrue(hasScheduleBasedEntry, "应该包含基于摊销记录的分录");

        // 验证汇总信息
        assertNotNull(response.getSummary());
        assertEquals(0, response.getSummary().getPaidAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testGenerateJournalEntriesWithPartialPayments() {
        // 准备部分付款信息
        JournalEntryGenerationRequest.ActualPayment payment = new JournalEntryGenerationRequest.ActualPayment();
        payment.setPaymentDate(LocalDate.of(2025, 1, 15));
        payment.setAmount(new BigDecimal("30000.00")); // 只付了部分金额
        payment.setPaymentMethod("银行转账");
        payment.setDescription("部分付款");
        
        testRequest.setActualPayments(Collections.singletonList(payment));

        // 准备摊销记录
        AmortizationSchedule schedule1 = new AmortizationSchedule();
        schedule1.setPeriodNumber(1);
        schedule1.setAmortizationDate(LocalDate.of(2025, 1, 31));
        schedule1.setAmortizationAmount(new BigDecimal("10000.00"));

        AmortizationSchedule schedule2 = new AmortizationSchedule();
        schedule2.setPeriodNumber(2);
        schedule2.setAmortizationDate(LocalDate.of(2025, 2, 28));
        schedule2.setAmortizationAmount(new BigDecimal("10000.00"));

        AmortizationSchedule schedule3 = new AmortizationSchedule();
        schedule3.setPeriodNumber(3);
        schedule3.setAmortizationDate(LocalDate.of(2025, 3, 31));
        schedule3.setAmortizationAmount(new BigDecimal("10000.00"));

        // Mock 行为
        when(contractRepository.findById(123L)).thenReturn(Optional.of(testContract));
        when(amortizationScheduleRepository.existsByContractId(123L)).thenReturn(true);
        when(amortizationScheduleRepository.findByContractIdOrderByPeriodNumber(123L))
                .thenReturn(Arrays.asList(schedule1, schedule2, schedule3));

        // 执行测试
        JournalEntryGenerationResponse response = contractService.generateJournalEntries(123L, testRequest);

        // 验证结果
        assertNotNull(response);
        
        // 应该包含付款分录
        boolean hasPaymentEntry = response.getJournalEntries().stream()
                .anyMatch(entry -> entry.getDescription().contains("预付款"));
        assertTrue(hasPaymentEntry, "应该包含付款确认分录");

        // 应该包含基于摊销记录的分录（对应已付款的期间）
        boolean hasScheduleBasedEntry = response.getJournalEntries().stream()
                .anyMatch(entry -> entry.getReference().contains("Amortization-Schedule"));
        assertTrue(hasScheduleBasedEntry, "应该包含基于摊销记录的分录");

        // 验证汇总信息
        assertNotNull(response.getSummary());
        assertEquals(0, response.getSummary().getPaidAmount().compareTo(new BigDecimal("30000.00")));
    }
}
