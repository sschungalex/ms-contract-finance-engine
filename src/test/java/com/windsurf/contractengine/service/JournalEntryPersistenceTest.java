package com.windsurf.contractengine.service;

import com.windsurf.contractengine.dto.JournalEntryGenerationRequest;
import com.windsurf.contractengine.dto.JournalEntryGenerationResponse;
import com.windsurf.contractengine.dto.JournalEntryQueryResponse;
import com.windsurf.contractengine.entity.Contract;
import com.windsurf.contractengine.entity.JournalEntry;
import com.windsurf.contractengine.entity.PaymentSchedule;
import com.windsurf.contractengine.enums.EntryStatus;
import com.windsurf.contractengine.enums.EntryType;
import com.windsurf.contractengine.enums.PaymentStatus;
import com.windsurf.contractengine.repository.AmortizationScheduleRepository;
import com.windsurf.contractengine.repository.ContractRepository;
import com.windsurf.contractengine.repository.JournalEntryRepository;
import com.windsurf.contractengine.repository.PaymentScheduleRepository;
import com.windsurf.contractengine.service.impl.ContractServiceImpl;
import com.windsurf.contractengine.util.ContractUpdateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * 会计分录持久化测试类
 */
@ExtendWith(MockitoExtension.class)
class JournalEntryPersistenceTest {

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

        // 创建实际付款信息
        JournalEntryGenerationRequest.ActualPayment payment = new JournalEntryGenerationRequest.ActualPayment();
        payment.setPaymentDate(LocalDate.of(2025, 1, 15));
        payment.setAmount(new BigDecimal("30000.00"));
        payment.setPaymentMethod("银行转账");
        payment.setDescription("首期付款");
        
        testRequest.setActualPayments(Collections.singletonList(payment));
    }

    @Test
    void testPersistActualPayments() {
        // Mock 行为
        when(contractRepository.findById(123L)).thenReturn(Optional.of(testContract));
        when(amortizationScheduleRepository.existsByContractId(123L)).thenReturn(false);
        when(paymentScheduleRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(journalEntryRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // 执行测试
        contractService.generateJournalEntries(123L, testRequest);

        // 验证付款记录持久化
        ArgumentCaptor<List<PaymentSchedule>> paymentCaptor = ArgumentCaptor.forClass(List.class);
        verify(paymentScheduleRepository).saveAll(paymentCaptor.capture());
        
        List<PaymentSchedule> savedPayments = paymentCaptor.getValue();
        assertNotNull(savedPayments);
        assertEquals(1, savedPayments.size());
        
        PaymentSchedule savedPayment = savedPayments.get(0);
        assertEquals(testContract, savedPayment.getContract());
        assertEquals(1, savedPayment.getPeriodNumber());
        assertEquals(LocalDate.of(2025, 1, 15), savedPayment.getActualDate());
        assertEquals(0, savedPayment.getActualAmount().compareTo(new BigDecimal("30000.00")));
        assertEquals("银行转账", savedPayment.getPaymentMethod());
        assertEquals("首期付款", savedPayment.getPaymentDescription());
        assertEquals(PaymentStatus.PAID, savedPayment.getStatus());
        assertEquals(0, savedPayment.getVarianceAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testPersistJournalEntries() {
        // Mock 行为
        when(contractRepository.findById(123L)).thenReturn(Optional.of(testContract));
        when(amortizationScheduleRepository.existsByContractId(123L)).thenReturn(false);
        when(paymentScheduleRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(journalEntryRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // 执行测试
        contractService.generateJournalEntries(123L, testRequest);

        // 验证会计分录持久化
        ArgumentCaptor<List<JournalEntry>> journalEntryCaptor = ArgumentCaptor.forClass(List.class);
        verify(journalEntryRepository).saveAll(journalEntryCaptor.capture());
        
        List<JournalEntry> savedEntries = journalEntryCaptor.getValue();
        assertNotNull(savedEntries);
        assertTrue(savedEntries.size() > 0);
        
        // 验证付款分录
        boolean hasPaymentEntry = savedEntries.stream()
                .anyMatch(entry -> EntryType.PAYMENT.equals(entry.getEntryType()));
        assertTrue(hasPaymentEntry, "应该包含付款分录");
        
        // 验证摊销分录
        boolean hasAmortizationEntry = savedEntries.stream()
                .anyMatch(entry -> EntryType.AMORTIZATION.equals(entry.getEntryType()));
        assertTrue(hasAmortizationEntry, "应该包含摊销分录");
        
        // 验证分录基本信息
        JournalEntry firstEntry = savedEntries.get(0);
        assertEquals(testContract, firstEntry.getContract());
        assertEquals(EntryStatus.POSTED, firstEntry.getStatus());
        assertEquals("system", firstEntry.getCreatedBy());
        assertTrue(firstEntry.getBalanced());
        assertNotNull(firstEntry.getEntryLines());
        assertTrue(firstEntry.getEntryLines().size() > 0);
    }

    @Test
    void testQueryJournalEntries() {
        // 准备测试数据
        JournalEntry mockEntry = new JournalEntry();
        mockEntry.setId(1L);
        mockEntry.setContract(testContract);
        mockEntry.setEntryId("JE-2025-001");
        mockEntry.setEntryType(EntryType.PAYMENT);
        mockEntry.setDescription("合同预付款确认");
        mockEntry.setTotalDr(new BigDecimal("30000.00"));
        mockEntry.setTotalCr(new BigDecimal("30000.00"));
        mockEntry.setBalanced(true);
        mockEntry.setStatus(EntryStatus.POSTED);
        mockEntry.setEntryLines(Collections.emptyList());

        // Mock 行为
        when(contractRepository.findById(123L)).thenReturn(Optional.of(testContract));
        when(journalEntryRepository.findByContractIdWithLines(123L))
                .thenReturn(Collections.singletonList(mockEntry));

        // 执行测试
        JournalEntryQueryResponse response = contractService.getJournalEntriesByContractId(123L);

        // 验证结果
        assertNotNull(response);
        assertEquals(123L, response.getContractId());
        assertNotNull(response.getJournalEntries());
        assertEquals(1, response.getJournalEntries().size());
        
        JournalEntryQueryResponse.JournalEntryItemDto entryDto = response.getJournalEntries().get(0);
        assertEquals(1L, entryDto.getId());
        assertEquals("JE-2025-001", entryDto.getEntryId());
        assertEquals("PAYMENT", entryDto.getEntryType());
        assertEquals("合同预付款确认", entryDto.getDescription());
        assertEquals(0, entryDto.getTotalDr().compareTo(new BigDecimal("30000.00")));
        assertEquals(0, entryDto.getTotalCr().compareTo(new BigDecimal("30000.00")));
        assertTrue(entryDto.getBalanced());
        assertEquals("POSTED", entryDto.getStatus());
        
        // 验证汇总信息
        assertNotNull(response.getSummary());
        assertEquals(1, response.getSummary().getTotalEntries());
        assertEquals(1, response.getSummary().getPaymentEntries());
        assertEquals(0, response.getSummary().getAmortizationEntries());
        assertEquals(0, response.getSummary().getTotalDebitAmount().compareTo(new BigDecimal("30000.00")));
        assertEquals(0, response.getSummary().getTotalCreditAmount().compareTo(new BigDecimal("30000.00")));
        assertTrue(response.getSummary().getBalanced());
    }

    @Test
    void testQueryJournalEntriesWithEmptyResult() {
        // Mock 行为
        when(contractRepository.findById(123L)).thenReturn(Optional.of(testContract));
        when(journalEntryRepository.findByContractIdWithLines(123L))
                .thenReturn(Collections.emptyList());

        // 执行测试
        JournalEntryQueryResponse response = contractService.getJournalEntriesByContractId(123L);

        // 验证结果
        assertNotNull(response);
        assertEquals(123L, response.getContractId());
        assertNotNull(response.getJournalEntries());
        assertEquals(0, response.getJournalEntries().size());
        
        // 验证汇总信息
        assertNotNull(response.getSummary());
        assertEquals(0, response.getSummary().getTotalEntries());
        assertEquals(0, response.getSummary().getPaymentEntries());
        assertEquals(0, response.getSummary().getAmortizationEntries());
        assertEquals(0, response.getSummary().getTotalDebitAmount().compareTo(BigDecimal.ZERO));
        assertEquals(0, response.getSummary().getTotalCreditAmount().compareTo(BigDecimal.ZERO));
        assertTrue(response.getSummary().getBalanced());
    }

    @Test
    void testMultiplePaymentsPersistence() {
        // 创建多笔付款
        JournalEntryGenerationRequest.ActualPayment payment1 = new JournalEntryGenerationRequest.ActualPayment();
        payment1.setPaymentDate(LocalDate.of(2025, 1, 15));
        payment1.setAmount(new BigDecimal("30000.00"));
        payment1.setPaymentMethod("银行转账");
        payment1.setDescription("首期付款");

        JournalEntryGenerationRequest.ActualPayment payment2 = new JournalEntryGenerationRequest.ActualPayment();
        payment2.setPaymentDate(LocalDate.of(2025, 4, 15));
        payment2.setAmount(new BigDecimal("30000.00"));
        payment2.setPaymentMethod("银行转账");
        payment2.setDescription("第二期付款");

        testRequest.setActualPayments(List.of(payment1, payment2));

        // Mock 行为
        when(contractRepository.findById(123L)).thenReturn(Optional.of(testContract));
        when(amortizationScheduleRepository.existsByContractId(123L)).thenReturn(false);
        when(paymentScheduleRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(journalEntryRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // 执行测试
        contractService.generateJournalEntries(123L, testRequest);

        // 验证多笔付款记录持久化
        ArgumentCaptor<List<PaymentSchedule>> paymentCaptor = ArgumentCaptor.forClass(List.class);
        verify(paymentScheduleRepository).saveAll(paymentCaptor.capture());
        
        List<PaymentSchedule> savedPayments = paymentCaptor.getValue();
        assertEquals(2, savedPayments.size());
        
        // 验证第一笔付款
        PaymentSchedule firstPayment = savedPayments.get(0);
        assertEquals(1, firstPayment.getPeriodNumber());
        assertEquals(LocalDate.of(2025, 1, 15), firstPayment.getActualDate());
        assertEquals(0, firstPayment.getActualAmount().compareTo(new BigDecimal("30000.00")));
        
        // 验证第二笔付款
        PaymentSchedule secondPayment = savedPayments.get(1);
        assertEquals(2, secondPayment.getPeriodNumber());
        assertEquals(LocalDate.of(2025, 4, 15), secondPayment.getActualDate());
        assertEquals(0, secondPayment.getActualAmount().compareTo(new BigDecimal("30000.00")));
    }
}
