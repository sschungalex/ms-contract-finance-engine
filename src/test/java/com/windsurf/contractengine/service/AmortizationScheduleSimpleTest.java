package com.windsurf.contractengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.windsurf.contractengine.dto.AmortizationScheduleResponse;
import com.windsurf.contractengine.dto.TimeElementsDto;
import com.windsurf.contractengine.entity.Contract;
import com.windsurf.contractengine.enums.ServicePeriodType;
import com.windsurf.contractengine.repository.AmortizationScheduleRepository;
import com.windsurf.contractengine.repository.ContractRepository;
import com.windsurf.contractengine.repository.JournalEntryRepository;
import com.windsurf.contractengine.repository.PaymentScheduleRepository;
import com.windsurf.contractengine.service.impl.ContractServiceImpl;
import com.windsurf.contractengine.util.ContractUpdateUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 摊销计划生成简化测试
 * 避免复杂的依赖注入问题
 */
@DisplayName("摊销计划生成简化测试")
class AmortizationScheduleSimpleTest {

    @Test
    @DisplayName("简单测试 - 按月摊销")
    void testMonthlyAmortization() {
        // 1. 创建 mock 对象
        ContractRepository contractRepository = mock(ContractRepository.class);
        AmortizationScheduleRepository amortizationScheduleRepository = mock(AmortizationScheduleRepository.class);
        JournalEntryRepository journalEntryRepository = mock(JournalEntryRepository.class);
        PaymentScheduleRepository paymentScheduleRepository = mock(PaymentScheduleRepository.class);
        ContractUpdateUtil contractUpdateUtil = mock(ContractUpdateUtil.class);

        // 2. 创建 service
        ContractService contractService = new ContractServiceImpl(contractRepository, amortizationScheduleRepository, 
                journalEntryRepository, paymentScheduleRepository, contractUpdateUtil);

        // 3. 准备测试数据
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setContractNumber("CT202501001");
        contract.setContractName("测试合同");
        contract.setTotalAmount(new BigDecimal("120000.00"));
        contract.setContractDate(LocalDate.of(2025, 1, 1));
        contract.setOriginalFilename("test_contract.pdf");
        contract.setServicePeriodType(ServicePeriodType.MONTHLY);
        contract.setServiceDuration(12);

        // 4. 设置 mock 行为
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        // 5. 执行测试
        AmortizationScheduleResponse response = contractService.generateAmortizationSchedule(1L);

        // 6. 验证结果
        assertNotNull(response, "响应不应为空");
        assertEquals(1L, response.getContractId(), "合同ID应该匹配");
        assertEquals("SERVICE_PERIOD", response.getAmortizationStrategy(), "应该使用服务周期策略");
        assertEquals(12, response.getAmortizationSchedule().size(), "应该生成12条记录");

        // 验证第一条记录
        var firstItem = response.getAmortizationSchedule().get(0);
        assertEquals(0, firstItem.getAmortizationAmount().compareTo(new BigDecimal("10000.00")), "每月摊销金额应为10000");
        assertEquals("2025-01", firstItem.getPeriod(), "期间格式应正确");
        assertNull(firstItem.getMilestone(), "服务周期策略不应有里程碑");

        System.out.println("✅ 按月摊销测试通过");
    }

    @Test
    @DisplayName("简单测试 - 交付节点摊销")
    void testDeliveryNodesAmortization() throws Exception {
        // 1. 创建 mock 对象
        ContractRepository contractRepository = mock(ContractRepository.class);
        AmortizationScheduleRepository amortizationScheduleRepository = mock(AmortizationScheduleRepository.class);
        JournalEntryRepository journalEntryRepository = mock(JournalEntryRepository.class);
        PaymentScheduleRepository paymentScheduleRepository = mock(PaymentScheduleRepository.class);
        ContractUpdateUtil contractUpdateUtil = mock(ContractUpdateUtil.class);

        // 2. 创建 service
        ContractService contractService = new ContractServiceImpl(contractRepository, amortizationScheduleRepository, 
                journalEntryRepository, paymentScheduleRepository, contractUpdateUtil);

        // 3. 准备测试数据
        Contract contract = new Contract();
        contract.setId(2L);
        contract.setContractNumber("CT202501002");
        contract.setContractName("测试合同-交付节点");
        contract.setTotalAmount(new BigDecimal("120000.00"));
        contract.setContractDate(LocalDate.of(2025, 1, 1));
        contract.setOriginalFilename("test_delivery.pdf");

        // 设置交付节点
        TimeElementsDto timeElements = new TimeElementsDto();
        List<TimeElementsDto.DeliveryNodeDto> nodes = new ArrayList<>();

        TimeElementsDto.DeliveryNodeDto node1 = new TimeElementsDto.DeliveryNodeDto();
        node1.setMilestone("项目启动");
        node1.setPercentage(30);
        node1.setDueDate("2025-01-15");
        nodes.add(node1);

        TimeElementsDto.DeliveryNodeDto node2 = new TimeElementsDto.DeliveryNodeDto();
        node2.setMilestone("中期交付");
        node2.setPercentage(40);
        node2.setDueDate("2025-06-15");
        nodes.add(node2);

        TimeElementsDto.DeliveryNodeDto node3 = new TimeElementsDto.DeliveryNodeDto();
        node3.setMilestone("项目完成");
        node3.setPercentage(30);
        node3.setDueDate("2025-12-15");
        nodes.add(node3);

        timeElements.setDeliveryNodes(nodes);

        ObjectMapper objectMapper = new ObjectMapper();
        String timeElementsJson = objectMapper.writeValueAsString(timeElements);
        contract.setTimeElements(timeElementsJson);

        // 4. 设置 mock 行为
        when(contractRepository.findById(2L)).thenReturn(Optional.of(contract));

        // 5. 执行测试
        AmortizationScheduleResponse response = contractService.generateAmortizationSchedule(2L);

        // 6. 验证结果
        assertNotNull(response, "响应不应为空");
        assertEquals(2L, response.getContractId(), "合同ID应该匹配");
        assertEquals("DELIVERY_NODES", response.getAmortizationStrategy(), "应该使用交付节点策略");
        assertEquals(3, response.getAmortizationSchedule().size(), "应该生成3条记录");

        // 验证第一个节点
        var item1 = response.getAmortizationSchedule().get(0);
        assertEquals("项目启动", item1.getMilestone(), "里程碑名称应匹配");
        assertEquals(30, item1.getPercentage(), "百分比应匹配");
        assertEquals(0, item1.getAmortizationAmount().compareTo(new BigDecimal("36000.00")), "金额应为120000*30%");

        // 验证第二个节点
        var item2 = response.getAmortizationSchedule().get(1);
        assertEquals("中期交付", item2.getMilestone());
        assertEquals(40, item2.getPercentage());
        assertEquals(0, item2.getAmortizationAmount().compareTo(new BigDecimal("48000.00")), "金额应为120000*40%");

        // 验证第三个节点
        var item3 = response.getAmortizationSchedule().get(2);
        assertEquals("项目完成", item3.getMilestone());
        assertEquals(30, item3.getPercentage());
        assertEquals(0, item3.getAmortizationAmount().compareTo(new BigDecimal("36000.00")), "金额应为120000*30%");

        System.out.println("✅ 交付节点摊销测试通过");
    }

    @Test
    @DisplayName("简单测试 - 按季度摊销")
    void testQuarterlyAmortization() {
        ContractRepository contractRepository = mock(ContractRepository.class);
        ContractUpdateUtil contractUpdateUtil = mock(ContractUpdateUtil.class);
        AmortizationScheduleRepository amortizationScheduleRepository = mock(AmortizationScheduleRepository.class);
        JournalEntryRepository journalEntryRepository = mock(JournalEntryRepository.class);
        PaymentScheduleRepository paymentScheduleRepository = mock(PaymentScheduleRepository.class);
        ContractService contractService = new ContractServiceImpl(contractRepository, amortizationScheduleRepository, 
                journalEntryRepository, paymentScheduleRepository, contractUpdateUtil);

        Contract contract = new Contract();
        contract.setId(3L);
        contract.setTotalAmount(new BigDecimal("120000.00"));
        contract.setContractDate(LocalDate.of(2025, 1, 1));
        contract.setOriginalFilename("test_quarterly.pdf");
        contract.setServicePeriodType(ServicePeriodType.QUARTERLY);
        contract.setServiceDuration(4); // 4个季度

        when(contractRepository.findById(3L)).thenReturn(Optional.of(contract));

        AmortizationScheduleResponse response = contractService.generateAmortizationSchedule(3L);

        assertNotNull(response);
        assertEquals(12, response.getAmortizationSchedule().size(), "4个季度应生成12个月的记录");
        assertEquals(0, response.getAmortizationSchedule().get(0).getAmortizationAmount().compareTo(new BigDecimal("10000.00")));

        System.out.println("✅ 按季度摊销测试通过");
    }

    @Test
    @DisplayName("简单测试 - 汇总信息验证")
    void testSummaryCalculation() {
        ContractRepository contractRepository = mock(ContractRepository.class);
        ContractUpdateUtil contractUpdateUtil = mock(ContractUpdateUtil.class);
        AmortizationScheduleRepository amortizationScheduleRepository = mock(AmortizationScheduleRepository.class);
        JournalEntryRepository journalEntryRepository = mock(JournalEntryRepository.class);
        PaymentScheduleRepository paymentScheduleRepository = mock(PaymentScheduleRepository.class);
        ContractService contractService = new ContractServiceImpl(contractRepository, amortizationScheduleRepository, 
                journalEntryRepository, paymentScheduleRepository, contractUpdateUtil);

        Contract contract = new Contract();
        contract.setId(4L);
        contract.setTotalAmount(new BigDecimal("120000.00"));
        contract.setContractDate(LocalDate.of(2025, 1, 1));
        contract.setOriginalFilename("test.pdf");
        contract.setServicePeriodType(ServicePeriodType.MONTHLY);
        contract.setServiceDuration(12);

        when(contractRepository.findById(4L)).thenReturn(Optional.of(contract));

        AmortizationScheduleResponse response = contractService.generateAmortizationSchedule(4L);

        // 验证汇总信息
        assertNotNull(response.getSummary());
        assertEquals(12, response.getSummary().getTotalRecords());
        assertEquals(0, response.getSummary().getTotalAmortizationAmount().compareTo(new BigDecimal("120000.00")));
        assertEquals(0, response.getSummary().getAverageMonthlyAmount().compareTo(new BigDecimal("10000.00")));
        assertEquals("2025-01", response.getSummary().getStartPeriod());
        assertEquals("2025-12", response.getSummary().getEndPeriod());

        System.out.println("✅ 汇总信息验证测试通过");
    }

    @Test
    @DisplayName("简单测试 - 计算依据验证")
    void testCalculationBasis() {
        ContractRepository contractRepository = mock(ContractRepository.class);
        ContractUpdateUtil contractUpdateUtil = mock(ContractUpdateUtil.class);
        AmortizationScheduleRepository amortizationScheduleRepository = mock(AmortizationScheduleRepository.class);
        JournalEntryRepository journalEntryRepository = mock(JournalEntryRepository.class);
        PaymentScheduleRepository paymentScheduleRepository = mock(PaymentScheduleRepository.class);
        ContractService contractService = new ContractServiceImpl(contractRepository, amortizationScheduleRepository, 
                journalEntryRepository, paymentScheduleRepository, contractUpdateUtil);

        Contract contract = new Contract();
        contract.setId(5L);
        contract.setTotalAmount(new BigDecimal("120000.00"));
        contract.setContractDate(LocalDate.of(2025, 1, 1));
        contract.setOriginalFilename("test.pdf");
        contract.setServicePeriodType(ServicePeriodType.MONTHLY);
        contract.setServiceDuration(12);

        when(contractRepository.findById(5L)).thenReturn(Optional.of(contract));

        AmortizationScheduleResponse response = contractService.generateAmortizationSchedule(5L);

        // 验证计算依据
        assertNotNull(response.getCalculationBasis());
        assertEquals(0, response.getCalculationBasis().getTotalAmount().compareTo(new BigDecimal("120000.00")));
        assertEquals("MONTHLY", response.getCalculationBasis().getServicePeriodType());
        assertEquals(12, response.getCalculationBasis().getServiceDuration());
        assertTrue(response.getCalculationBasis().getAmortizationFormula().contains("服务期间月数"));
        assertTrue(response.getCalculationBasis().getMonthlyAmount().contains("120000.00"));

        System.out.println("✅ 计算依据验证测试通过");
    }
}
