package com.windsurf.contractengine.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windsurf.contractengine.dto.*;
import com.windsurf.contractengine.entity.Contract;
import com.windsurf.contractengine.enums.AIProcessingStatus;
import com.windsurf.contractengine.enums.AmortizationStrategy;
import com.windsurf.contractengine.enums.ContractStatus;
import com.windsurf.contractengine.enums.ServicePeriodType;
import com.windsurf.contractengine.exception.ResourceNotFoundException;
import com.windsurf.contractengine.repository.ContractRepository;
import com.windsurf.contractengine.service.ContractService;
import com.windsurf.contractengine.util.ContractUpdateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 合同服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final ContractUpdateUtil contractUpdateUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public ContractResponse uploadContract(MultipartFile file, ContractCreateRequest request) {
        log.info("开始上传合同文件: {}", file.getOriginalFilename());

        // TODO: 实现文件上传和AI处理逻辑
        throw new UnsupportedOperationException("上传合同功能待实现");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<ContractListResponse> getUploadedContracts(Integer page, Integer size,
                                                                  String status, LocalDate startDate,
                                                                  LocalDate endDate) {
        log.info("查询已上传合同列表: page={}, size={}, status={}, startDate={}, endDate={}",
                page, size, status, startDate, endDate);

        Pageable pageable = getPageable(page, size);

        // 解析AI处理状态枚举
        // 如果status为null或空字符串，则aiStatus保持为null，表示查询所有状态
        AIProcessingStatus aiStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                aiStatus = AIProcessingStatus.valueOf(status.toUpperCase());
                log.debug("过滤AI处理状态: {}", aiStatus);
            } catch (IllegalArgumentException e) {
                log.warn("无效的AI处理状态: {}，将查询所有状态的合同", status);
                // 无效状态时，aiStatus保持为null，查询所有状态
            }
        } else {
            log.debug("未指定状态参数，查询所有状态的合同");
        }

        // 查询合同列表
        Page<Contract> contractPage = contractRepository.findUploadedContracts(
                aiStatus, startDate, endDate, pageable);

        // 转换为列表项响应DTO
        List<ContractListItemResponse> items = contractPage.getContent().stream()
                .map(this::convertToListItemResponse)
                .collect(Collectors.toList());

        ContractListResponse data = ContractListResponse.of(
                items,
                pageable.getPageNumber() + 1, // API使用1-based页码
                pageable.getPageSize(),
                contractPage.getTotalElements()
        );
        return ApiResponse.success(data);
    }

    private static Pageable getPageable(Integer page, Integer size) {
        // 验证分页参数
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 100) {
            size = 10;
        }

        // 创建分页参数，按上传时间倒序排序
        Pageable pageable = PageRequest.of(
                page - 1, // Spring Data JPA的页码从0开始
                size,
                Sort.by(Sort.Direction.DESC, "uploadTime", "createdAt")
        );
        return pageable;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ContractResponse> getContracts(String contractType, String status,
                                                       String keyword, Pageable pageable) {
        log.info("查询合同列表: contractType={}, status={}, keyword={}, page={}, size={}",
                contractType, status, keyword, pageable.getPageNumber(), pageable.getPageSize());

        // 解析状态枚举
        ContractStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = ContractStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("无效的合同状态: {}", status);
            }
        }

        // 创建按上传时间倒序排序的分页参数
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "uploadTime", "createdAt")
        );

        // 查询合同列表
        Page<Contract> contractPage = contractRepository.findContractsWithFilters(
                contractType, statusEnum, keyword, sortedPageable);

        // 转换为响应DTO
        List<ContractResponse> contractResponses = contractPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(
                contractResponses,
                contractPage.getNumber(),
                contractPage.getSize(),
                contractPage.getTotalElements()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponse getContractById(Long id) {
        log.info("查询合同完整详情: id={}", id);

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("合同不存在: " + id));

        return convertToResponse(contract);
    }

    @Override
    @Transactional
    public ContractUpdateResponse updateContract(Long id, ContractUpdateRequest request) {
        log.info("更新合同信息: id={}", id);

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("合同不存在: " + id));

        // 检查合同状态是否允许修改
        if (contract.getStatus() == ContractStatus.CANCELLED ||
            contract.getStatus() == ContractStatus.TERMINATED) {
            throw new IllegalStateException("合同状态不允许修改: " + contract.getStatus());
        }

        List<String> updatedFields = contractUpdateUtil.updateContractFields(contract, request);
        contractUpdateUtil.updateComplexFields(contract, request, updatedFields);

        // 保存更新后的合同
        Contract updatedContract = contractRepository.save(contract);
        log.info("合同更新成功: id={}, updatedAt={}, updatedFields={}",
                updatedContract.getId(), updatedContract.getUpdatedAt(), updatedFields);

        // 构建并返回更新响应
        return ContractUpdateResponse.builder()
                .contractId(updatedContract.getId())
                .message("合同数据更新成功")
                .updatedTime(updatedContract.getUpdatedAt())
                .updatedFields(updatedFields)
                .build();
    }

    @Override
    @Transactional
    public void deleteContract(Long id) {
        log.info("删除合同: id={}", id);

        if (!contractRepository.existsById(id)) {
            throw new ResourceNotFoundException("合同不存在: " + id);
        }

        contractRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void extractFields(Long id) {
        log.info("执行字段提取: id={}", id);

        // TODO: 实现字段提取逻辑
        throw new UnsupportedOperationException("字段提取功能待实现");
    }

    @Override
    @Transactional(readOnly = true)
    public Object getExtractedFields(Long id) {
        log.info("查询提取字段: id={}", id);

        // TODO: 实现获取提取字段逻辑
        throw new UnsupportedOperationException("获取提取字段功能待实现");
    }

    @Override
    @Transactional
    public void generatePaymentSchedule(Long id) {
        log.info("生成支付计划: id={}", id);

        // TODO: 实现生成支付计划逻辑
        throw new UnsupportedOperationException("生成支付计划功能待实现");
    }

    @Override
    @Transactional(readOnly = true)
    public AmortizationScheduleResponse generateAmortizationSchedule(Long id) {
        log.info("生成摊销计划: id={}", id);

        // 1. 查询合同
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("合同不存在: " + id));

        // 2. 验证必要字段
        if (contract.getTotalAmount() == null) {
            throw new IllegalStateException("合同缺少总金额信息");
        }

        // 3. 解析 timeElements 并决定摊销策略
        TimeElementsDto timeElements = parseTimeElements(contract.getTimeElements());
        AmortizationStrategy strategy = determineAmortizationStrategy(
                timeElements,
                contract.getServicePeriodType(),
                contract.getServiceDuration()
        );

        log.info("使用摊销策略: {}", strategy);

        // 4. 获取开始日期
        LocalDate startDate = contract.getContractDate() != null
                ? contract.getContractDate()
                : LocalDate.now();

        // 5. 根据策略计算摊销计划
        List<AmortizationScheduleItemResponse> scheduleItems;
        AmortizationCalculationBasisResponse calculationBasis;

        if (strategy == AmortizationStrategy.DELIVERY_NODES) {
            // 按交付节点策略
            scheduleItems = calculateAmortizationByDeliveryNodes(
                    contract.getTotalAmount(),
                    timeElements.getDeliveryNodes(),
                    startDate
            );
            calculationBasis = buildCalculationBasisForDeliveryNodes(
                    contract.getTotalAmount(),
                    timeElements.getDeliveryNodes()
            );
        } else {
            // 按服务周期策略 - 优先使用 timeElements，否则使用实体字段
            ServicePeriodType periodType = extractServicePeriodType(timeElements, contract.getServicePeriodType());
            Integer duration = extractServiceDuration(timeElements, contract.getServiceDuration());
            
            scheduleItems = calculateAmortizationByServicePeriod(
                    contract.getTotalAmount(),
                    periodType,
                    duration,
                    startDate
            );
            calculationBasis = buildCalculationBasisForServicePeriod(
                    contract.getTotalAmount(),
                    periodType,
                    duration
            );
        }

        // 6. 构建合同信息
        AmortizationContractInfoResponse contractInfo = buildContractInfo(contract, startDate);

        // 7. 构建汇总信息
        AmortizationScheduleSummaryResponse summary = buildSummary(scheduleItems);

        // 8. 构建响应
        return AmortizationScheduleResponse.builder()
                .contractId(id)
                .amortizationStrategy(strategy.name())
                .contractInfo(contractInfo)
                .amortizationSchedule(scheduleItems)
                .summary(summary)
                .generatedTime(java.time.LocalDateTime.now())
                .calculationBasis(calculationBasis)
                .build();
    }

    /**
     * 解析时间要素 JSON
     */
    private TimeElementsDto parseTimeElements(String timeElementsJson) {
        if (timeElementsJson == null || timeElementsJson.trim().isEmpty()) {
            //todo:throw exception
            return null;
        }

        try {
            return objectMapper.readValue(timeElementsJson, TimeElementsDto.class);
        } catch (Exception e) {
            log.warn("解析 timeElements 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 决定摊销策略
     * 优先级：deliveryNodes > servicePeriod > 实体字段
     */
    private com.windsurf.contractengine.enums.AmortizationStrategy determineAmortizationStrategy(
            TimeElementsDto timeElements,
            com.windsurf.contractengine.enums.ServicePeriodType servicePeriodType,
            Integer serviceDuration) {

        // 1. 优先检查 timeElements 中的 deliveryNodes
        if (timeElements != null && timeElements.getDeliveryNodes() != null
                && !timeElements.getDeliveryNodes().isEmpty()) {
            log.info("检测到 deliveryNodes，使用交付节点摊销策略");
            return com.windsurf.contractengine.enums.AmortizationStrategy.DELIVERY_NODES;
        }

        // 2. 检查 timeElements 中的 servicePeriod
        if (timeElements != null && timeElements.getServicePeriod() != null
                && timeElements.getServicePeriod().getDuration() != null) {
            log.info("检测到 servicePeriod，使用服务周期摊销策略");
            return com.windsurf.contractengine.enums.AmortizationStrategy.SERVICE_PERIOD;
        }

        // 3. 使用实体字段
        if (servicePeriodType != null && serviceDuration != null) {
            log.info("使用实体字段，使用服务周期摊销策略");
            return AmortizationStrategy.SERVICE_PERIOD;
        }

        throw new IllegalStateException("合同缺少必要的时间要素信息，无法生成摊销计划");
    }

    /**
     * 提取服务周期类型 - 优先使用 timeElements，否则使用实体字段
     */
    private ServicePeriodType extractServicePeriodType(TimeElementsDto timeElements, ServicePeriodType entityValue) {
        if (timeElements != null && timeElements.getServicePeriod() != null 
                && timeElements.getServicePeriod().getType() != null) {
            try {
                return ServicePeriodType.valueOf(timeElements.getServicePeriod().getType());
            } catch (IllegalArgumentException e) {
                log.warn("无效的服务周期类型: {}", timeElements.getServicePeriod().getType());
            }
        }
        return entityValue;
    }

    /**
     * 提取服务周期时长 - 优先使用 timeElements，否则使用实体字段
     */
    private Integer extractServiceDuration(TimeElementsDto timeElements, Integer entityValue) {
        if (timeElements != null && timeElements.getServicePeriod() != null 
                && timeElements.getServicePeriod().getDuration() != null) {
            return timeElements.getServicePeriod().getDuration();
        }
        return entityValue;
    }

    /**
     * 按服务周期计算摊销计划明细
     */
    private List<AmortizationScheduleItemResponse> calculateAmortizationByServicePeriod(
            BigDecimal totalAmount,
            ServicePeriodType periodType,
            Integer duration,
            LocalDate startDate) {

        List<AmortizationScheduleItemResponse> items = new ArrayList<>();

        // 计算月度摊销金额
        BigDecimal monthlyAmount = calculateMonthlyAmount(totalAmount, periodType, duration);

        // 根据服务周期类型生成摊销明细
        int totalMonths = calculateTotalMonths(periodType, duration);

        for (int i = 0; i < totalMonths; i++) {
            LocalDate periodStart = startDate.plusMonths(i);
            LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);

            String period = periodStart.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
            String accrualPeriod = periodStart.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    + " 至 "
                    + periodEnd.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            items.add(AmortizationScheduleItemResponse.builder()
                    .period(period)
                    .accrualPeriod(accrualPeriod)
                    .accountingPeriod(period)
                    .amortizationAmount(monthlyAmount)
                    .build());
        }

        return items;
    }

    /**
     * 按交付节点计算摊销计划明细
     */
    private List<AmortizationScheduleItemResponse> calculateAmortizationByDeliveryNodes(
            java.math.BigDecimal totalAmount,
            List<TimeElementsDto.DeliveryNodeDto> deliveryNodes,
            LocalDate startDate) {

        List<AmortizationScheduleItemResponse> items = new ArrayList<>();

        // 验证百分比总和
        int totalPercentage = deliveryNodes.stream()
                .mapToInt(TimeElementsDto.DeliveryNodeDto::getPercentage)
                .sum();

        if (totalPercentage != 100) {
            log.warn("交付节点百分比总和不等于100: {}", totalPercentage);
            //todo: throw exception -> 前端添加验证？
        }

        // 按交付节点生成摊销明细
        for (TimeElementsDto.DeliveryNodeDto node : deliveryNodes) {
            // 计算该节点的摊销金额
            java.math.BigDecimal nodeAmount = totalAmount
                    .multiply(java.math.BigDecimal.valueOf(node.getPercentage()))
                    .divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

            // 解析交付日期
            LocalDate dueDate = node.getDueDate() != null
                    ? LocalDate.parse(node.getDueDate())
                    : startDate;

            String period = dueDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
            String accrualPeriod = dueDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            items.add(AmortizationScheduleItemResponse.builder()
                    .period(period)
                    .accrualPeriod(accrualPeriod)
                    .accountingPeriod(period)
                    .amortizationAmount(nodeAmount)
                    .milestone(node.getMilestone())
                    .percentage(node.getPercentage())
                    .build());
        }

        // 按日期排序
        items.sort((a, b) -> a.getAccrualPeriod().compareTo(b.getAccrualPeriod()));

        return items;
    }

    /**
     * 计算月度摊销金额
     */
    private java.math.BigDecimal calculateMonthlyAmount(
            java.math.BigDecimal totalAmount,
            com.windsurf.contractengine.enums.ServicePeriodType periodType,
            Integer duration) {

        int totalMonths = calculateTotalMonths(periodType, duration);
        return totalAmount.divide(
                java.math.BigDecimal.valueOf(totalMonths),
                2,
                java.math.RoundingMode.HALF_UP
        );
    }

    /**
     * 计算总月数
     */
    private int calculateTotalMonths(
            com.windsurf.contractengine.enums.ServicePeriodType periodType,
            Integer duration) {

        switch (periodType) {
            case MONTHLY:
                return duration;
            case QUARTERLY:
                return duration * 3;
            case YEARLY:
                return duration * 12;
            case ONCE:
                return duration != null ? duration : 1;
            case WEEKLY:
                // 按周转换为月（4周约等于1月）
                return (int) Math.ceil(duration / 4.0);
            default:
                return duration;
        }
    }

    /**
     * 构建合同信息
     */
    private AmortizationContractInfoResponse buildContractInfo(Contract contract, LocalDate startDate) {
        AmortizationContractInfoResponse.ServicePeriodResponse servicePeriod = null;
        
        // 只有在服务周期类型存在时才构建 servicePeriod
        if (contract.getServicePeriodType() != null) {
            String description = buildServicePeriodDescription(
                    contract.getServicePeriodType(),
                    contract.getServiceDuration()
            );

            servicePeriod = AmortizationContractInfoResponse.ServicePeriodResponse.builder()
                    .type(contract.getServicePeriodType().name())
                    .duration(contract.getServiceDuration())
                    .description(description)
                    .build();
        }

        return AmortizationContractInfoResponse.builder()
                .fileName(contract.getOriginalFilename())
                .totalAmount(contract.getTotalAmount())
                .contractStartDate(startDate.toString())
                .servicePeriod(servicePeriod)
                .build();
    }

    /**
     * 构建服务周期描述
     */
    private String buildServicePeriodDescription(
            ServicePeriodType periodType,
            Integer duration) {

        String unit = periodType.getDescription();
        switch (periodType) {
            case MONTHLY:
                return String.format("按月服务，共%d个月", duration);
            case QUARTERLY:
                return String.format("按季度服务，共%d个季度", duration);
            case YEARLY:
                return String.format("按年服务，共%d年", duration);
            case ONCE:
                return "一次性服务";
            case WEEKLY:
                return String.format("按周服务，共%d周", duration);
            default:
                return String.format("服务周期：%d%s", duration, unit);
        }
    }

    /**
     * 构建汇总信息
     */
    private AmortizationScheduleSummaryResponse buildSummary(
            List<AmortizationScheduleItemResponse> items) {

        if (items.isEmpty()) {
            return AmortizationScheduleSummaryResponse.builder()
                    .totalRecords(0)
                    .totalAmortizationAmount(java.math.BigDecimal.ZERO)
                    .averageMonthlyAmount(java.math.BigDecimal.ZERO)
                    .build();
        }

        java.math.BigDecimal total = items.stream()
                .map(AmortizationScheduleItemResponse::getAmortizationAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal average = total.divide(
                java.math.BigDecimal.valueOf(items.size()),
                2,
                java.math.RoundingMode.HALF_UP
        );

        return AmortizationScheduleSummaryResponse.builder()
                .totalRecords(items.size())
                .totalAmortizationAmount(total)
                .averageMonthlyAmount(average)
                .startPeriod(items.get(0).getPeriod())
                .endPeriod(items.get(items.size() - 1).getPeriod())
                .build();
    }

    /**
     * 构建服务周期策略的计算依据
     */
    private AmortizationCalculationBasisResponse buildCalculationBasisForServicePeriod(
            java.math.BigDecimal totalAmount,
            com.windsurf.contractengine.enums.ServicePeriodType periodType,
            Integer duration) {

        int totalMonths = calculateTotalMonths(periodType, duration);
        java.math.BigDecimal monthlyAmount = totalAmount.divide(
                java.math.BigDecimal.valueOf(totalMonths),
                2,
                java.math.RoundingMode.HALF_UP
        );

        String formula = buildAmortizationFormula(periodType);
        String calculation = String.format("%s ÷ %d = %s",
                totalAmount.toString(),
                totalMonths,
                monthlyAmount.toString()
        );

        return AmortizationCalculationBasisResponse.builder()
                .totalAmount(totalAmount)
                .servicePeriodType(periodType.name())
                .serviceDuration(duration)
                .amortizationFormula(formula)
                .monthlyAmount(calculation)
                .build();
    }

    /**
     * 构建交付节点策略的计算依据
     */
    private AmortizationCalculationBasisResponse buildCalculationBasisForDeliveryNodes(
            java.math.BigDecimal totalAmount,
            List<TimeElementsDto.DeliveryNodeDto> deliveryNodes) {

        String formula = "按交付节点百分比摊销";
        StringBuilder calculation = new StringBuilder();

        for (int i = 0; i < deliveryNodes.size(); i++) {
            TimeElementsDto.DeliveryNodeDto node = deliveryNodes.get(i);
            java.math.BigDecimal nodeAmount = totalAmount
                    .multiply(java.math.BigDecimal.valueOf(node.getPercentage()))
                    .divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

            if (i > 0) {
                calculation.append("; ");
            }
            calculation.append(String.format("%s: %s × %d%% = %s",
                    node.getMilestone(),
                    totalAmount.toString(),
                    node.getPercentage(),
                    nodeAmount.toString()
            ));
        }

        return AmortizationCalculationBasisResponse.builder()
                .totalAmount(totalAmount)
                .servicePeriodType("DELIVERY_NODES")
                .serviceDuration(deliveryNodes.size())
                .amortizationFormula(formula)
                .monthlyAmount(calculation.toString())
                .build();
    }

    /**
     * 构建摊销公式说明
     */
    private String buildAmortizationFormula(com.windsurf.contractengine.enums.ServicePeriodType periodType) {
        switch (periodType) {
            case MONTHLY:
                return "合同总金额 ÷ 服务期间月数";
            case QUARTERLY:
                return "合同总金额 ÷ 服务期间季度数 ÷ 3";
            case YEARLY:
                return "合同总金额 ÷ 服务期间年数 ÷ 12";
            case ONCE:
                return "合同总金额 ÷ 服务期间总月数";
            case WEEKLY:
                return "合同总金额 ÷ 服务期间周数 × 4";
            default:
                return "合同总金额 ÷ 服务期间";
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Object getSchedules(Long id) {
        log.info("查询合同计划: id={}", id);

        // TODO: 实现获取计划逻辑
        throw new UnsupportedOperationException("获取计划功能待实现");
    }

    @Override
    @Transactional
    public void generateJournalEntries(Long id) {
        log.info("生成会计分录: id={}", id);

        // TODO: 实现生成会计分录逻辑
        throw new UnsupportedOperationException("生成会计分录功能待实现");
    }

    /**
     * 转换合同实体为响应DTO
     * 包含AI提取的结构化数据
     */
    private ContractResponse convertToResponse(Contract contract) {
        ContractResponse response = new ContractResponse();

        // 基本信息
        response.setId(contract.getId());
        response.setContractNumber(contract.getContractNumber());
        response.setContractName(contract.getContractName());
        response.setContractType(contract.getContractType());
        response.setCounterparty(contract.getCounterparty());
        response.setStatus(contract.getStatus());
        response.setAiProcessingStatus(contract.getAiProcessingStatus());
        response.setAiConfidence(contract.getAiConfidence());
        response.setOriginalFilename(contract.getOriginalFilename());
        response.setFileSize(contract.getFileSize());
        response.setUploadTime(contract.getUploadTime());

        // 财务信息
        response.setPaymentMethod(contract.getPaymentMethod());
        response.setTotalAmount(contract.getTotalAmount());
        response.setCurrency(contract.getCurrency());
        response.setPaymentFrequency(contract.getPaymentFrequency());
        response.setTaxRate(contract.getTaxRate());
        response.setRemarks(contract.getRemarks());

        // 日期信息
        response.setContractDate(contract.getContractDate());
        response.setStartDate(contract.getStartDate() != null ? contract.getStartDate().toLocalDate() : null);
        response.setEndDate(contract.getEndDate() != null ? contract.getEndDate().toLocalDate() : null);

        // 服务信息
        response.setUnitPrice(contract.getUnitPrice());
        response.setQuantity(contract.getQuantity());
        response.setServicePeriodType(contract.getServicePeriodType() != null ?
                contract.getServicePeriodType().name() : null);
        response.setServiceDuration(contract.getServiceDuration());
        response.setServiceDescription(contract.getServiceDescription());

        // 审计信息
        response.setCreatedAt(contract.getCreatedAt());
        response.setUpdatedAt(contract.getUpdatedAt());
        response.setCreatedBy(contract.getCreatedBy());

        // 解析JSON字段
        parseJsonFields(contract, response);

        return response;
    }

    /**
     * 转换合同实体为列表项响应DTO
     */
    private ContractListItemResponse convertToListItemResponse(Contract contract) {
        ContractListItemResponse response = new ContractListItemResponse();
        response.setContractId(contract.getId());
        response.setOriginalFileName(contract.getOriginalFilename());

        // 设置状态 - 使用AI处理状态
        if (contract.getAiProcessingStatus() != null) {
            response.setStatus(contract.getAiProcessingStatus().name());
        } else {
            response.setStatus("PROCESSING");
        }

        // 设置上传时间 - 优先使用uploadTime，否则使用createdAt
        response.setUploadTime(contract.getUploadTime() != null ?
                contract.getUploadTime() : contract.getCreatedAt());

        response.setTotalAmount(contract.getTotalAmount());
        response.setPaymentMethod(contract.getPaymentMethod());

        return response;
    }

    /**
     * 解析合同实体中的JSON字段
     */
    private void parseJsonFields(Contract contract, ContractResponse response) {
        try {
            // 解析付款日期列表
            if (contract.getPaymentDates() != null && !contract.getPaymentDates().isEmpty()) {
                List<LocalDate> paymentDates = objectMapper.readValue(
                        contract.getPaymentDates(),
                        new TypeReference<List<LocalDate>>() {}
                );
                response.setPaymentDates(paymentDates);
            }

            // 解析合同当事方
            if (contract.getParties() != null && !contract.getParties().isEmpty()) {
                List<String> parties = objectMapper.readValue(
                        contract.getParties(),
                        new TypeReference<List<String>>() {}
                );
                response.setParties(parties);
            }

            // 解析金额要素
            if (contract.getAmountElements() != null && !contract.getAmountElements().isEmpty()) {
                ContractResponse.AmountElements amountElements = objectMapper.readValue(
                        contract.getAmountElements(),
                        ContractResponse.AmountElements.class
                );
                response.setAmountElements(amountElements);
            }

            // 解析时间要素
            if (contract.getTimeElements() != null && !contract.getTimeElements().isEmpty()) {
                ContractResponse.TimeElements timeElements = objectMapper.readValue(
                        contract.getTimeElements(),
                        ContractResponse.TimeElements.class
                );
                response.setTimeElements(timeElements);
            }
        } catch (Exception e) {
            log.warn("解析合同JSON字段失败: contractId={}, error={}", contract.getId(), e.getMessage());
        }
    }

    // 工具方法已移至 ContractUpdateUtil 类，简化了验空逻辑
}
