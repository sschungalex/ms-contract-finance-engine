package com.windsurf.contractengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.windsurf.contractengine.dto.AmortizationScheduleResponse;
import com.windsurf.contractengine.dto.TimeElementsDto;
import com.windsurf.contractengine.entity.Contract;
import com.windsurf.contractengine.enums.AmortizationStrategy;
import com.windsurf.contractengine.enums.ServicePeriodType;
import com.windsurf.contractengine.exception.ResourceNotFoundException;
import com.windsurf.contractengine.repository.ContractRepository;
import com.windsurf.contractengine.util.ContractUpdateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 摊销计划生成测试
 * 测试两种摊销策略的生成逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("摊销计划生成测试")
class AmortizationScheduleGenerationTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractUpdateUtil contractUpdateUtil;

    private ContractService contractService;
    private ObjectMapper objectMapper;
    private Contract testContract;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // 手动创建 service 实例，因为 ContractServiceImpl 的 objectMapper 是内部初始化的
        contractService = new com.windsurf.contractengine.service.impl.ContractServiceImpl(
                contractRepository, 
                contractUpdateUtil
        );
        
        testContract = new Contract();
        testContract.setId(1L);
        testContract.setContractNumber("CT202501001");
        testContract.setContractName("测试合同");
        testContract.setTotalAmount(new BigDecimal("120000.00"));
        testContract.setContractDate(LocalDate.of(2025, 1, 1));
        testContract.setOriginalFilename("test_contract.pdf");
    }

    @Test
    @DisplayName("测试按服务周期策略生成摊销计划 - 按月")
    void testGenerateAmortizationSchedule_ServicePeriod_Monthly() throws Exception {
        // 准备数据 - 使用实体字段
        testContract.setServicePeriodType(ServicePeriodType.MONTHLY);
        testContract.setServiceDuration(12);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        // 执行
        AmortizationScheduleResponse response = contractService.generateAmortizationSchedule(1L);

        // 验证
        assertNotNull(response);
        assertEquals(1L, response.getContractId());
        assertEquals(AmortizationStrategy.SERVICE_PERIOD.name(), response.getAmortizationStrategy());
        
        // 验证摊销明细
        assertNotNull(response.getAmortizationSchedule());
        assertEquals(12, response.getAmortizationSchedule().size());
        
        // 验证每月摊销金额
        BigDecimal expectedMonthlyAmount = new BigDecimal("10000.00");
        response.getAmortizationSchedule().forEach(item -> {
            assertEquals(expectedMonthlyAmount, item.getAmortizationAmount());
            assertNotNull(item.getPeriod());
            assertNotNull(item.getAccrualPeriod());
            assertNull(item.getMilestone()); // 服务周期策略不应有里程碑
        });

        // 验证汇总信息
        assertNotNull(response.getSummary());
        assertEquals(12, response.getSummary().getTotalRecords());
        assertEquals(new BigDecimal("120000.00"), response.getSummary().getTotalAmortizationAmount());
        assertEquals(new BigDecimal("10000.00"), response.getSummary().getAverageMonthlyAmount());
        assertEquals("2025-01", response.getSummary().getStartPeriod());
        assertEquals("2025-12", response.getSummary().getEndPeriod());

        // 验证计算依据
        assertNotNull(response.getCalculationBasis());
        assertEquals(new BigDecimal("120000.00"), response.getCalculationBasis().getTotalAmount());
        assertEquals("MONTHLY", response.getCalculationBasis().getServicePeriodType());
        assertEquals(12, response.getCalculationBasis().getServiceDuration());
        assertTrue(response.getCalculationBasis().getAmortizationFormula().contains("服务期间月数"));
    }

    @Test
    @DisplayName("测试按服务周期策略生成摊销计划 - 按季度")
    void testGenerateAmortizationSchedule_ServicePeriod_Quarterly() throws Exception {
        // 准备数据
        testContract.setServicePeriodType(ServicePeriodType.QUARTERLY);
        testContract.setServiceDuration(4); // 4个季度 = 12个月

        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        // 执行
        AmortizationScheduleResponse response = contractService.generateAmortizationSchedule(1L);

        // 验证
        assertNotNull(response);
        assertEquals(AmortizationStrategy.SERVICE_PERIOD.name(), response.getAmortizationStrategy());
        
        // 4个季度 = 12个月
        assertEquals(12, response.getAmortizationSchedule().size());
        
        // 验证每月摊销金额 (120000 / 12 = 10000)
        BigDecimal expectedMonthlyAmount = new BigDecimal("10000.00");
        response.getAmortizationSchedule().forEach(item -> {
            assertEquals(expectedMonthlyAmount, item.getAmortizationAmount());
        });
    }

    @Test
    @DisplayName("测试按服务周期策略生成摊销计划 - 按年")
    void testGenerateAmortizationSchedule_ServicePeriod_Yearly() throws Exception {
        // 准备数据
        testContract.setServicePeriodType(ServicePeriodType.YEARLY);
        testContract.setServiceDuration(1); // 1年 = 12个月

        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        // 执行
        AmortizationScheduleResponse response = contractService.generateAmortizationSchedule(1L);

        // 验证
        assertNotNull(response);
        assertEquals(12, response.getAmortizationSchedule().size());
        
        BigDecimal expectedMonthlyAmount = new BigDecimal("10000.00");
        response.getAmortizationSchedule().forEach(item -> {
            assertEquals(expectedMonthlyAmount, item.getAmortizationAmount());
        });
    }

    @Test
    @DisplayName("测试按交付节点策略生成摊销计划")
    void testGenerateAmortizationSchedule_DeliveryNodes() throws Exception {
        // 准备数据 - 使用 timeElements JSON
        TimeElementsDto timeElements = new TimeElementsDto();
        
        List<TimeElementsDto.DeliveryNodeDto> deliveryNodes = new ArrayList<>();
        
        TimeElementsDto.DeliveryNodeDto node1 = new TimeElementsDto.DeliveryNodeDto();
        node1.setMilestone("项目启动");
        node1.setPercentage(30);
        node1.setDueDate("2025-01-15");
        deliveryNodes.add(node1);
        
        TimeElementsDto.DeliveryNodeDto node2 = new TimeElementsDto.DeliveryNodeDto();
        node2.setMilestone("中期交付");
        node2.setPercentage(40);
        node2.setDueDate("2025-06-15");
        deliveryNodes.add(node2);
        
        TimeElementsDto.DeliveryNodeDto node3 = new TimeElementsDto.DeliveryNodeDto();
        node3.setMilestone("项目完成");
        node3.setPercentage(30);
        node3.setDueDate("2025-12-15");
        deliveryNodes.add(node3);
        
        timeElements.setDeliveryNodes(deliveryNodes);
        
        String timeElementsJson = objectMapper.writeValueAsString(timeElements);
        testContract.setTimeElements(timeElementsJson);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        // 执行
        AmortizationScheduleResponse response = contractService.generateAmortizationSchedule(1L);

        // 验证
        assertNotNull(response);
        assertEquals(1L, response.getContractId());
        assertEquals(AmortizationStrategy.DELIVERY_NODES.name(), response.getAmortizationStrategy());
        
        // 验证摊销明细
        assertNotNull(response.getAmortizationSchedule());
        assertEquals(3, response.getAmortizationSchedule().size());
        
        // 验证第一个节点
        var item1 = response.getAmortizationSchedule().get(0);
        assertEquals("项目启动", item1.getMilestone());
        assertEquals(30, item1.getPercentage());
        assertEquals(new BigDecimal("36000.00"), item1.getAmortizationAmount()); // 120000 * 30%
        assertEquals("2025-01-15", item1.getAccrualPeriod());
        
        // 验证第二个节点
        var item2 = response.getAmortizationSchedule().get(1);
        assertEquals("中期交付", item2.getMilestone());
        assertEquals(40, item2.getPercentage());
        assertEquals(new BigDecimal("48000.00"), item2.getAmortizationAmount()); // 120000 * 40%
        assertEquals("2025-06-15", item2.getAccrualPeriod());
        
        // 验证第三个节点
        var item3 = response.getAmortizationSchedule().get(2);
        assertEquals("项目完成", item3.getMilestone());
        assertEquals(30, item3.getPercentage());
        assertEquals(new BigDecimal("36000.00"), item3.getAmortizationAmount()); // 120000 * 30%
        assertEquals("2025-12-15", item3.getAccrualPeriod());

        // 验证汇总信息
        assertNotNull(response.getSummary());
        assertEquals(3, response.getSummary().getTotalRecords());
        assertEquals(new BigDecimal("120000.00"), response.getSummary().getTotalAmortizationAmount());

        // 验证计算依据
        assertNotNull(response.getCalculationBasis());
        assertEquals("DELIVERY_NODES", response.getCalculationBasis().getServicePeriodType());
        assertTrue(response.getCalculationBasis().getAmortizationFormula().contains("交付节点"));
        assertTrue(response.getCalculationBasis().getMonthlyAmount().contains("项目启动"));
        assertTrue(response.getCalculationBasis().getMonthlyAmount().contains("30%"));
    }

    @Test
    @DisplayName("测试策略优先级 - deliveryNodes 优先于 servicePeriod")
    void testStrategyPriority_DeliveryNodesOverServicePeriod() throws Exception {
        // 准备数据 - 同时设置 deliveryNodes 和 servicePeriod
        TimeElementsDto timeElements = new TimeElementsDto();
        
        // 设置 servicePeriod
        TimeElementsDto.ServicePeriodDto servicePeriod = new TimeElementsDto.ServicePeriodDto();
        servicePeriod.setType("MONTHLY");
        servicePeriod.setDuration(12);
        servicePeriod.setDescription("按月服务，共12个月");
        timeElements.setServicePeriod(servicePeriod);
        
        // 设置 deliveryNodes
        List<TimeElementsDto.DeliveryNodeDto> deliveryNodes = new ArrayList<>();
        TimeElementsDto.DeliveryNodeDto node = new TimeElementsDto.DeliveryNodeDto();
        node.setMilestone("一次性交付");
        node.setPercentage(100);
        node.setDueDate("2025-06-30");
        deliveryNodes.add(node);
        timeElements.setDeliveryNodes(deliveryNodes);
        
        String timeElementsJson = objectMapper.writeValueAsString(timeElements);
        testContract.setTimeElements(timeElementsJson);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        // 执行
        AmortizationScheduleResponse response = contractService.generateAmortizationSchedule(1L);

        // 验证 - 应该使用 DELIVERY_NODES 策略
        assertEquals(AmortizationStrategy.DELIVERY_NODES.name(), response.getAmortizationStrategy());
        assertEquals(1, response.getAmortizationSchedule().size());
        assertEquals("一次性交付", response.getAmortizationSchedule().get(0).getMilestone());
    }

    @Test
    @DisplayName("测试合同不存在时抛出异常")
    void testGenerateAmortizationSchedule_ContractNotFound() {
        when(contractRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            contractService.generateAmortizationSchedule(999L);
        });
    }

    @Test
    @DisplayName("测试缺少总金额时抛出异常")
    void testGenerateAmortizationSchedule_MissingTotalAmount() {
        testContract.setTotalAmount(null);
        testContract.setServicePeriodType(ServicePeriodType.MONTHLY);
        testContract.setServiceDuration(12);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            contractService.generateAmortizationSchedule(1L);
        });
        
        assertTrue(exception.getMessage().contains("总金额"));
    }

    @Test
    @DisplayName("测试缺少时间要素时抛出异常")
    void testGenerateAmortizationSchedule_MissingTimeElements() {
        // 不设置任何时间要素
        testContract.setServicePeriodType(null);
        testContract.setServiceDuration(null);
        testContract.setTimeElements(null);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            contractService.generateAmortizationSchedule(1L);
        });
        
        assertTrue(exception.getMessage().contains("时间要素"));
    }

    @Test
    @DisplayName("测试使用 timeElements 中的 servicePeriod")
    void testGenerateAmortizationSchedule_TimeElementsServicePeriod() throws Exception {
        // 准备数据 - 只在 timeElements 中设置 servicePeriod
        TimeElementsDto timeElements = new TimeElementsDto();
        
        TimeElementsDto.ServicePeriodDto servicePeriod = new TimeElementsDto.ServicePeriodDto();
        servicePeriod.setType("MONTHLY");
        servicePeriod.setDuration(6);
        servicePeriod.setDescription("按月服务，共6个月");
        timeElements.setServicePeriod(servicePeriod);
        
        String timeElementsJson = objectMapper.writeValueAsString(timeElements);
        testContract.setTimeElements(timeElementsJson);
        
        // 不设置实体字段
        testContract.setServicePeriodType(null);
        testContract.setServiceDuration(null);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        // 执行
        AmortizationScheduleResponse response = contractService.generateAmortizationSchedule(1L);

        // 验证
        assertEquals(AmortizationStrategy.SERVICE_PERIOD.name(), response.getAmortizationStrategy());
        assertEquals(6, response.getAmortizationSchedule().size());
        
        // 验证每月摊销金额 (120000 / 6 = 20000)
        BigDecimal expectedMonthlyAmount = new BigDecimal("20000.00");
        response.getAmortizationSchedule().forEach(item -> {
            assertEquals(expectedMonthlyAmount, item.getAmortizationAmount());
        });
    }

    @Test
    @DisplayName("测试交付节点百分比不等于100%的警告")
    void testGenerateAmortizationSchedule_DeliveryNodesPercentageNotHundred() throws Exception {
        // 准备数据 - 百分比总和不等于100
        TimeElementsDto timeElements = new TimeElementsDto();
        
        List<TimeElementsDto.DeliveryNodeDto> deliveryNodes = new ArrayList<>();
        
        TimeElementsDto.DeliveryNodeDto node1 = new TimeElementsDto.DeliveryNodeDto();
        node1.setMilestone("第一期");
        node1.setPercentage(50);
        node1.setDueDate("2025-03-01");
        deliveryNodes.add(node1);
        
        TimeElementsDto.DeliveryNodeDto node2 = new TimeElementsDto.DeliveryNodeDto();
        node2.setMilestone("第二期");
        node2.setPercentage(40); // 总和 = 90%，不等于100%
        node2.setDueDate("2025-06-01");
        deliveryNodes.add(node2);
        
        timeElements.setDeliveryNodes(deliveryNodes);
        
        String timeElementsJson = objectMapper.writeValueAsString(timeElements);
        testContract.setTimeElements(timeElementsJson);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        // 执行 - 应该生成但会有警告日志
        AmortizationScheduleResponse response = contractService.generateAmortizationSchedule(1L);

        // 验证 - 仍然能生成，但金额会按实际百分比计算
        assertNotNull(response);
        assertEquals(2, response.getAmortizationSchedule().size());
        assertEquals(new BigDecimal("60000.00"), response.getAmortizationSchedule().get(0).getAmortizationAmount());
        assertEquals(new BigDecimal("48000.00"), response.getAmortizationSchedule().get(1).getAmortizationAmount());
    }

    @Test
    @DisplayName("测试合同信息构建")
    void testContractInfoBuilding() throws Exception {
        testContract.setServicePeriodType(ServicePeriodType.MONTHLY);
        testContract.setServiceDuration(12);
        testContract.setOriginalFilename("test_contract.pdf");

        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        AmortizationScheduleResponse response = contractService.generateAmortizationSchedule(1L);

        // 验证合同信息
        assertNotNull(response.getContractInfo());
        assertEquals("test_contract.pdf", response.getContractInfo().getFileName());
        assertEquals(new BigDecimal("120000.00"), response.getContractInfo().getTotalAmount());
        assertEquals("2025-01-01", response.getContractInfo().getContractStartDate());
        
        assertNotNull(response.getContractInfo().getServicePeriod());
        assertEquals("MONTHLY", response.getContractInfo().getServicePeriod().getType());
        assertEquals(12, response.getContractInfo().getServicePeriod().getDuration());
        assertTrue(response.getContractInfo().getServicePeriod().getDescription().contains("12个月"));
    }

    @Test
    @DisplayName("测试生成时间字段")
    void testGeneratedTimeField() throws Exception {
        testContract.setServicePeriodType(ServicePeriodType.MONTHLY);
        testContract.setServiceDuration(12);

        when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

        AmortizationScheduleResponse response = contractService.generateAmortizationSchedule(1L);

        // 验证生成时间
        assertNotNull(response.getGeneratedTime());
        assertTrue(response.getGeneratedTime().isBefore(java.time.LocalDateTime.now().plusSeconds(1)));
    }
}
