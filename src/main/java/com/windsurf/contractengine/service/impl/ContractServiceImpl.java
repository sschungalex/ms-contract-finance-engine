package com.windsurf.contractengine.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windsurf.contractengine.dto.*;
import com.windsurf.contractengine.entity.AmortizationSchedule;
import com.windsurf.contractengine.entity.Contract;
import com.windsurf.contractengine.entity.JournalEntry;
import com.windsurf.contractengine.entity.JournalEntryLine;
import com.windsurf.contractengine.entity.PaymentSchedule;
import com.windsurf.contractengine.enums.AIProcessingStatus;
import com.windsurf.contractengine.enums.AmortizationStrategy;
import com.windsurf.contractengine.enums.ContractStatus;
import com.windsurf.contractengine.enums.EntryStatus;
import com.windsurf.contractengine.enums.EntryType;
import com.windsurf.contractengine.enums.PaymentStatus;
import com.windsurf.contractengine.enums.ServicePeriodType;
import com.windsurf.contractengine.exception.ResourceNotFoundException;
import com.windsurf.contractengine.repository.AmortizationScheduleRepository;
import com.windsurf.contractengine.repository.ContractRepository;
import com.windsurf.contractengine.repository.JournalEntryRepository;
import com.windsurf.contractengine.repository.PaymentScheduleRepository;
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
    private final AmortizationScheduleRepository amortizationScheduleRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final PaymentScheduleRepository paymentScheduleRepository;
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
    public JournalEntryGenerationResponse generateJournalEntries(Long id, JournalEntryGenerationRequest request) {
        log.info("生成会计分录: id={}, contractId={}", id, request.getContractInfo().getContractId());

        // 验证合同存在
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("合同不存在: " + id));

        // 验证请求中的合同ID与路径参数一致
        if (!id.equals(request.getContractInfo().getContractId())) {
            throw new IllegalArgumentException("路径参数合同ID与请求体中合同ID不一致");
        }

        // 检查是否存在摊销记录
        boolean hasAmortizationSchedule = amortizationScheduleRepository.existsByContractId(id);
        log.info("合同{}是否存在摊销记录: {}", id, hasAmortizationSchedule);

        // 生成会计分录
        return generateJournalEntriesInternal(contract, request, hasAmortizationSchedule);
    }

    /**
     * 内部方法：生成会计分录的具体实现
     */
    private JournalEntryGenerationResponse generateJournalEntriesInternal(Contract contract,
                                                                          JournalEntryGenerationRequest request,
                                                                          boolean hasAmortizationSchedule) {
        JournalEntryGenerationResponse response = new JournalEntryGenerationResponse();
        response.setContractId(contract.getId());
        response.setGeneratedTime(java.time.ZonedDateTime.now());

        List<JournalEntryGenerationResponse.JournalEntryDto> journalEntries = new ArrayList<>();
        
        // 1. 处理付款确认分录
        if (request.getActualPayments() != null && !request.getActualPayments().isEmpty()) {
            // 先持久化实际付款记录
            List<PaymentSchedule> savedPayments = persistActualPayments(contract, request.getActualPayments());
            log.info("持久化了{}笔实际付款记录", savedPayments.size());
            
            // 生成付款确认分录
            journalEntries.addAll(generatePaymentEntries(request));
            log.info("生成了{}笔付款确认分录", request.getActualPayments().size());
        }
        
        // 2. 处理摊销分录
        boolean shouldIncludeAccruals = request.getGenerateOptions() == null || 
                request.getGenerateOptions().getIncludeAccruals() == null || 
                request.getGenerateOptions().getIncludeAccruals();
                
        if (shouldIncludeAccruals) {
            if (hasAmortizationSchedule) {
                // 使用现有摊销记录生成分录
                journalEntries.addAll(generateAmortizationEntriesFromSchedule(contract.getId(), request));
                log.info("基于现有摊销记录生成摊销分录");
            } else {
                // 使用请求信息计算摊销分录
                journalEntries.addAll(generateAmortizationEntries(request));
                log.info("基于请求信息计算摊销分录");
            }
        }

        // 3. 持久化会计分录
        List<JournalEntry> savedJournalEntries = persistJournalEntries(contract, journalEntries);
        log.info("持久化了{}笔会计分录", savedJournalEntries.size());

        response.setJournalEntries(journalEntries);
        response.setSummary(calculateSummary(request, journalEntries, hasAmortizationSchedule));
        response.setAccountingPrinciples(getAccountingPrinciples());
        response.setGlAccountMapping(getGlAccountMapping());

        return response;
    }

    /**
     * 生成付款确认分录
     */
    private List<JournalEntryGenerationResponse.JournalEntryDto> generatePaymentEntries(JournalEntryGenerationRequest request) {
        List<JournalEntryGenerationResponse.JournalEntryDto> entries = new ArrayList<>();
        
        int entryIndex = 1;
        for (JournalEntryGenerationRequest.ActualPayment payment : request.getActualPayments()) {
            JournalEntryGenerationResponse.JournalEntryDto entry = new JournalEntryGenerationResponse.JournalEntryDto();
            
            entry.setEntryId(String.format("JE-%d-%03d", java.time.LocalDate.now().getYear(), entryIndex));
            entry.setBookingDate(payment.getPaymentDate());
            entry.setDescription("合同预付款确认");
            entry.setReference(String.format("Contract-%d-Payment-%d", 
                    request.getContractInfo().getContractId(), entryIndex));
            
            List<JournalEntryGenerationResponse.JournalEntryLineDto> lines = new ArrayList<>();
            
            // 借方：预付账款
            JournalEntryGenerationResponse.JournalEntryLineDto drLine = new JournalEntryGenerationResponse.JournalEntryLineDto();
            drLine.setLineNumber(1);
            drLine.setBookingDate(payment.getPaymentDate());
            drLine.setGlAccount("1221");
            drLine.setGlAccountName("预付账款");
            drLine.setEnteredDr(payment.getAmount());
            drLine.setEnteredCr(BigDecimal.ZERO);
            drLine.setDescription("确认预付服务费");
            lines.add(drLine);
            
            // 贷方：活期存款
            JournalEntryGenerationResponse.JournalEntryLineDto crLine = new JournalEntryGenerationResponse.JournalEntryLineDto();
            crLine.setLineNumber(2);
            crLine.setBookingDate(payment.getPaymentDate());
            crLine.setGlAccount("1001");
            crLine.setGlAccountName("活期存款");
            crLine.setEnteredDr(BigDecimal.ZERO);
            crLine.setEnteredCr(payment.getAmount());
            crLine.setDescription(payment.getPaymentMethod() + "付款");
            lines.add(crLine);
            
            entry.setLines(lines);
            entry.setTotalDr(payment.getAmount());
            entry.setTotalCr(payment.getAmount());
            entry.setBalanced(true);
            
            entries.add(entry);
            entryIndex++;
        }
        
        return entries;
    }

    /**
     * 生成费用摊销分录
     */
    private List<JournalEntryGenerationResponse.JournalEntryDto> generateAmortizationEntries(JournalEntryGenerationRequest request) {
        List<JournalEntryGenerationResponse.JournalEntryDto> entries = new ArrayList<>();
        
        // 计算月度摊销金额
        BigDecimal totalAmount = request.getContractInfo().getTotalAmount();
        Integer duration = request.getContractInfo().getPaymentPeriod().getDuration();
        BigDecimal monthlyAmortization = totalAmount.divide(BigDecimal.valueOf(duration), 2, BigDecimal.ROUND_HALF_UP);
        
        // 获取最早付款日期作为摊销开始日期
        LocalDate startDate = request.getActualPayments().stream()
                .map(JournalEntryGenerationRequest.ActualPayment::getPaymentDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
        
        int entryIndex = request.getActualPayments().size() + 1;
        
        // 生成每月摊销分录（这里简化为生成前几个月的示例）
        for (int month = 0; month < Math.min(duration, 3); month++) {
            LocalDate amortizationDate = startDate.plusMonths(month).withDayOfMonth(
                    startDate.plusMonths(month).lengthOfMonth()); // 月末
            
            JournalEntryGenerationResponse.JournalEntryDto entry = new JournalEntryGenerationResponse.JournalEntryDto();
            
            entry.setEntryId(String.format("JE-%d-%03d", amortizationDate.getYear(), entryIndex));
            entry.setBookingDate(amortizationDate);
            entry.setDescription(String.format("%d月份服务费用摊销", amortizationDate.getMonthValue()));
            entry.setReference(String.format("Contract-%d-Amortization-%d-%02d", 
                    request.getContractInfo().getContractId(), 
                    amortizationDate.getYear(), 
                    amortizationDate.getMonthValue()));
            
            List<JournalEntryGenerationResponse.JournalEntryLineDto> lines = new ArrayList<>();
            
            // 借方：服务费用
            JournalEntryGenerationResponse.JournalEntryLineDto drLine = new JournalEntryGenerationResponse.JournalEntryLineDto();
            drLine.setLineNumber(1);
            drLine.setBookingDate(amortizationDate);
            drLine.setGlAccount("6001");
            drLine.setGlAccountName("服务费用");
            drLine.setEnteredDr(monthlyAmortization);
            drLine.setEnteredCr(BigDecimal.ZERO);
            drLine.setDescription(String.format("%d月份服务费摊销", amortizationDate.getMonthValue()));
            lines.add(drLine);
            
            // 贷方：预付账款
            JournalEntryGenerationResponse.JournalEntryLineDto crLine = new JournalEntryGenerationResponse.JournalEntryLineDto();
            crLine.setLineNumber(2);
            crLine.setBookingDate(amortizationDate);
            crLine.setGlAccount("1221");
            crLine.setGlAccountName("预付账款");
            crLine.setEnteredDr(BigDecimal.ZERO);
            crLine.setEnteredCr(monthlyAmortization);
            crLine.setDescription("预付账款摊销");
            lines.add(crLine);
            
            entry.setLines(lines);
            entry.setTotalDr(monthlyAmortization);
            entry.setTotalCr(monthlyAmortization);
            entry.setBalanced(true);
            
            entries.add(entry);
            entryIndex++;
        }
        
        return entries;
    }

    /**
     * 基于现有摊销记录生成费用摊销分录
     */
    private List<JournalEntryGenerationResponse.JournalEntryDto> generateAmortizationEntriesFromSchedule(Long contractId, JournalEntryGenerationRequest request) {
        List<JournalEntryGenerationResponse.JournalEntryDto> entries = new ArrayList<>();
        
        // 查询合同的摊销记录
        List<AmortizationSchedule> schedules = amortizationScheduleRepository.findByContractIdOrderByPeriodNumber(contractId);
        
        if (schedules.isEmpty()) {
            log.warn("合同{}没有找到摊销记录", contractId);
            return entries;
        }
        
        log.info("找到{}条摊销记录", schedules.size());
        
        // 计算已付款的期间数（如果有实际付款信息）
        int paidPeriods = calculatePaidPeriods(request, schedules);
        
        int entryIndex = (request.getActualPayments() != null ? request.getActualPayments().size() : 0) + 1;
        
        for (int i = 0; i < schedules.size(); i++) {
            AmortizationSchedule schedule = schedules.get(i);
            
            // 如果没有实际付款信息，或者当前期间在已付款期间内，则生成摊销分录
            if (request.getActualPayments() == null || request.getActualPayments().isEmpty() || i < paidPeriods) {
                JournalEntryGenerationResponse.JournalEntryDto entry = new JournalEntryGenerationResponse.JournalEntryDto();
                
                entry.setEntryId(String.format("JE-%d-%03d", schedule.getAmortizationDate().getYear(), entryIndex));
                entry.setBookingDate(schedule.getAmortizationDate());
                entry.setDescription(String.format("%s服务费用摊销", schedule.getPeriod() != null ? schedule.getPeriod() : ("第" + schedule.getPeriodNumber() + "期")));
                entry.setReference(String.format("Contract-%d-Amortization-Schedule-%d", contractId, schedule.getPeriodNumber()));
                
                List<JournalEntryGenerationResponse.JournalEntryLineDto> lines = new ArrayList<>();
                
                // 借方：服务费用
                JournalEntryGenerationResponse.JournalEntryLineDto drLine = new JournalEntryGenerationResponse.JournalEntryLineDto();
                drLine.setLineNumber(1);
                drLine.setBookingDate(schedule.getAmortizationDate());
                drLine.setGlAccount("6001");
                drLine.setGlAccountName("服务费用");
                drLine.setEnteredDr(schedule.getAmortizationAmount());
                drLine.setEnteredCr(BigDecimal.ZERO);
                drLine.setDescription(String.format("%s服务费摊销", schedule.getPeriod() != null ? schedule.getPeriod() : ("第" + schedule.getPeriodNumber() + "期")));
                lines.add(drLine);
                
                // 贷方：预付账款
                JournalEntryGenerationResponse.JournalEntryLineDto crLine = new JournalEntryGenerationResponse.JournalEntryLineDto();
                crLine.setLineNumber(2);
                crLine.setBookingDate(schedule.getAmortizationDate());
                crLine.setGlAccount("1221");
                crLine.setGlAccountName("预付账款");
                crLine.setEnteredDr(BigDecimal.ZERO);
                crLine.setEnteredCr(schedule.getAmortizationAmount());
                crLine.setDescription("预付账款摊销");
                lines.add(crLine);
                
                entry.setLines(lines);
                entry.setTotalDr(schedule.getAmortizationAmount());
                entry.setTotalCr(schedule.getAmortizationAmount());
                entry.setBalanced(true);
                
                entries.add(entry);
                entryIndex++;
            }
        }
        
        return entries;
    }

    /**
     * 计算已付款对应的期间数
     */
    private int calculatePaidPeriods(JournalEntryGenerationRequest request, List<AmortizationSchedule> schedules) {
        if (request.getActualPayments() == null || request.getActualPayments().isEmpty()) {
            return schedules.size(); // 没有付款信息，返回所有期间
        }
        
        // 计算总付款金额
        BigDecimal totalPaidAmount = request.getActualPayments().stream()
                .map(JournalEntryGenerationRequest.ActualPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 计算对应的期间数
        BigDecimal accumulatedAmount = BigDecimal.ZERO;
        int paidPeriods = 0;
        
        for (AmortizationSchedule schedule : schedules) {
            accumulatedAmount = accumulatedAmount.add(schedule.getAmortizationAmount());
            paidPeriods++;
            
            if (accumulatedAmount.compareTo(totalPaidAmount) >= 0) {
                break;
            }
        }
        
        return paidPeriods;
    }

    /**
     * 计算汇总信息
     */
    private JournalEntryGenerationResponse.SummaryInfo calculateSummary(
            JournalEntryGenerationRequest request, 
            List<JournalEntryGenerationResponse.JournalEntryDto> journalEntries,
            boolean hasAmortizationSchedule) {
        
        JournalEntryGenerationResponse.SummaryInfo summary = new JournalEntryGenerationResponse.SummaryInfo();
        
        // 计算付款总额
        BigDecimal paidAmount = request.getActualPayments() != null ? 
                request.getActualPayments().stream()
                        .map(JournalEntryGenerationRequest.ActualPayment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
        
        // 计算已摊销金额（从摊销分录中计算）
        BigDecimal amortizedAmount = journalEntries.stream()
                .filter(entry -> entry.getDescription().contains("摊销"))
                .map(JournalEntryGenerationResponse.JournalEntryDto::getTotalDr)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 计算借贷总额
        BigDecimal totalDrAmount = journalEntries.stream()
                .map(JournalEntryGenerationResponse.JournalEntryDto::getTotalDr)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCrAmount = journalEntries.stream()
                .map(JournalEntryGenerationResponse.JournalEntryDto::getTotalCr)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 统计分录数量
        long paymentEntries = journalEntries.stream()
                .filter(entry -> entry.getDescription().contains("预付款"))
                .count();
        
        long amortizationEntries = journalEntries.stream()
                .filter(entry -> entry.getDescription().contains("摊销"))
                .count();
        
        summary.setTotalEntries(journalEntries.size());
        summary.setTotalPaymentEntries((int) paymentEntries);
        summary.setTotalAmortizationEntries((int) amortizationEntries);
        summary.setTotalDrAmount(totalDrAmount);
        summary.setTotalCrAmount(totalCrAmount);
        summary.setContractTotalAmount(request.getContractInfo().getTotalAmount());
        summary.setPaidAmount(paidAmount);
        summary.setRemainingAmount(request.getContractInfo().getTotalAmount().subtract(paidAmount));
        summary.setAmortizedAmount(amortizedAmount);
        summary.setPrepaidBalance(paidAmount.subtract(amortizedAmount));
        
        return summary;
    }

    /**
     * 获取会计准则说明
     */
    private JournalEntryGenerationResponse.AccountingPrinciples getAccountingPrinciples() {
        JournalEntryGenerationResponse.AccountingPrinciples principles = new JournalEntryGenerationResponse.AccountingPrinciples();
        principles.setPaymentRecognition("实际付款日期确认预付账款");
        principles.setExpenseRecognition("按服务期间摊销确认费用");
        principles.setBalancingRule("借贷必须平衡");
        principles.setAccrualBasis("权责发生制");
        return principles;
    }

    /**
     * 获取科目映射关系
     */
    private java.util.Map<String, String> getGlAccountMapping() {
        java.util.Map<String, String> mapping = new java.util.HashMap<>();
        mapping.put("1001", "活期存款 (银行账户)");
        mapping.put("1221", "预付账款 (资产科目)");
        mapping.put("2201", "应付账款 (负债科目)");
        mapping.put("6001", "服务费用 (费用科目)");
        mapping.put("6999", "其他费用 (差额调整)");
        return mapping;
    }

    /**
     * 持久化实际付款记录
     */
    private List<PaymentSchedule> persistActualPayments(Contract contract, List<JournalEntryGenerationRequest.ActualPayment> actualPayments) {
        List<PaymentSchedule> paymentSchedules = new ArrayList<>();
        
        for (int i = 0; i < actualPayments.size(); i++) {
            JournalEntryGenerationRequest.ActualPayment payment = actualPayments.get(i);
            
            PaymentSchedule paymentSchedule = new PaymentSchedule();
            paymentSchedule.setContract(contract);
            paymentSchedule.setPeriodNumber(i + 1);
            paymentSchedule.setScheduledDate(payment.getPaymentDate()); // 使用实际付款日期作为计划日期
            paymentSchedule.setScheduledAmount(payment.getAmount()); // 使用实际付款金额作为计划金额
            paymentSchedule.setActualDate(payment.getPaymentDate());
            paymentSchedule.setActualAmount(payment.getAmount());
            paymentSchedule.setPaymentMethod(payment.getPaymentMethod());
            paymentSchedule.setPaymentDescription(payment.getDescription());
            paymentSchedule.setStatus(PaymentStatus.PAID);
            paymentSchedule.setVarianceAmount(BigDecimal.ZERO); // 实际与计划相同，差额为0
            paymentSchedule.setRemarks("系统自动生成的实际付款记录");
            
            paymentSchedules.add(paymentSchedule);
        }
        
        return paymentScheduleRepository.saveAll(paymentSchedules);
    }

    /**
     * 持久化会计分录
     */
    private List<JournalEntry> persistJournalEntries(Contract contract, List<JournalEntryGenerationResponse.JournalEntryDto> journalEntryDtos) {
        List<JournalEntry> journalEntries = new ArrayList<>();
        
        for (JournalEntryGenerationResponse.JournalEntryDto dto : journalEntryDtos) {
            JournalEntry journalEntry = new JournalEntry();
            
            // 设置基本信息
            journalEntry.setContract(contract);
            journalEntry.setEntryNumber(dto.getEntryId());
            journalEntry.setEntryId(dto.getEntryId());
            journalEntry.setEntryDate(dto.getBookingDate());
            journalEntry.setBookingDate(dto.getBookingDate());
            journalEntry.setDescription(dto.getDescription());
            journalEntry.setReference(dto.getReference());
            journalEntry.setTotalAmount(dto.getTotalDr());
            journalEntry.setTotalDr(dto.getTotalDr());
            journalEntry.setTotalCr(dto.getTotalCr());
            journalEntry.setBalanced(dto.getBalanced());
            journalEntry.setStatus(EntryStatus.POSTED); // 设置为已过账状态
            journalEntry.setCreatedBy("system");
            
            // 判断分录类型
            if (dto.getDescription().contains("预付款")) {
                journalEntry.setEntryType(EntryType.PAYMENT);
            } else if (dto.getDescription().contains("摊销")) {
                journalEntry.setEntryType(EntryType.AMORTIZATION);
            } else {
                journalEntry.setEntryType(EntryType.ADJUSTMENT);
            }
            
            // 设置分录明细
            List<JournalEntryLine> entryLines = new ArrayList<>();
            for (JournalEntryGenerationResponse.JournalEntryLineDto lineDto : dto.getLines()) {
                JournalEntryLine entryLine = new JournalEntryLine();
                
                entryLine.setJournalEntry(journalEntry);
                entryLine.setLineNumber(lineDto.getLineNumber());
                entryLine.setBookingDate(lineDto.getBookingDate());
                entryLine.setGlAccount(lineDto.getGlAccount());
                entryLine.setGlAccountName(lineDto.getGlAccountName());
                entryLine.setDebitAmount(lineDto.getEnteredDr());
                entryLine.setCreditAmount(lineDto.getEnteredCr());
                entryLine.setEnteredDr(lineDto.getEnteredDr());
                entryLine.setEnteredCr(lineDto.getEnteredCr());
                entryLine.setDescription(lineDto.getDescription());
                
                entryLines.add(entryLine);
            }
            
            journalEntry.setEntryLines(entryLines);
            journalEntries.add(journalEntry);
        }
        
        return journalEntryRepository.saveAll(journalEntries);
    }

    @Override
    @Transactional(readOnly = true)
    public JournalEntryQueryResponse getJournalEntriesByContractId(Long id) {
        log.info("查询合同会计分录: id={}", id);

        // 验证合同存在
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("合同不存在: " + id));

        // 查询会计分录
        List<JournalEntry> journalEntries = journalEntryRepository.findByContractIdWithLines(id);

        // 转换为响应DTO
        return convertToJournalEntryQueryResponse(contract, journalEntries);
    }

    /**
     * 转换会计分录实体为查询响应DTO
     */
    private JournalEntryQueryResponse convertToJournalEntryQueryResponse(Contract contract, List<JournalEntry> journalEntries) {
        JournalEntryQueryResponse response = new JournalEntryQueryResponse();
        response.setContractId(contract.getId());

        // 转换分录列表
        List<JournalEntryQueryResponse.JournalEntryItemDto> entryItems = new ArrayList<>();
        for (JournalEntry entry : journalEntries) {
            JournalEntryQueryResponse.JournalEntryItemDto itemDto = new JournalEntryQueryResponse.JournalEntryItemDto();
            
            itemDto.setId(entry.getId());
            itemDto.setEntryNumber(entry.getEntryNumber());
            itemDto.setEntryId(entry.getEntryId());
            itemDto.setEntryDate(entry.getEntryDate());
            itemDto.setBookingDate(entry.getBookingDate());
            itemDto.setEntryType(entry.getEntryType() != null ? entry.getEntryType().name() : null);
            itemDto.setDescription(entry.getDescription());
            itemDto.setReference(entry.getReference());
            itemDto.setTotalAmount(entry.getTotalAmount());
            itemDto.setTotalDr(entry.getTotalDr());
            itemDto.setTotalCr(entry.getTotalCr());
            itemDto.setBalanced(entry.getBalanced());
            itemDto.setStatus(entry.getStatus() != null ? entry.getStatus().name() : null);
            itemDto.setPaymentScheduleId(entry.getPaymentScheduleId());
            itemDto.setAmortizationScheduleId(entry.getAmortizationScheduleId());
            itemDto.setRemarks(entry.getRemarks());
            itemDto.setCreatedAt(entry.getCreatedAt());
            itemDto.setCreatedBy(entry.getCreatedBy());

            // 转换分录明细行
            List<JournalEntryQueryResponse.JournalEntryLineItemDto> lineItems = new ArrayList<>();
            for (JournalEntryLine line : entry.getEntryLines()) {
                JournalEntryQueryResponse.JournalEntryLineItemDto lineDto = new JournalEntryQueryResponse.JournalEntryLineItemDto();
                
                lineDto.setId(line.getId());
                lineDto.setLineNumber(line.getLineNumber());
                lineDto.setBookingDate(line.getBookingDate());
                lineDto.setGlAccount(line.getGlAccount());
                lineDto.setGlAccountName(line.getGlAccountName());
                lineDto.setDebitAmount(line.getDebitAmount());
                lineDto.setCreditAmount(line.getCreditAmount());
                lineDto.setEnteredDr(line.getEnteredDr());
                lineDto.setEnteredCr(line.getEnteredCr());
                lineDto.setDescription(line.getDescription());
                lineDto.setAuxiliaryInfo(line.getAuxiliaryInfo());
                lineDto.setCreatedAt(line.getCreatedAt());
                
                lineItems.add(lineDto);
            }
            
            itemDto.setEntryLines(lineItems);
            entryItems.add(itemDto);
        }
        
        response.setJournalEntries(entryItems);

        // 计算汇总统计
        response.setSummary(calculateJournalEntrySummary(journalEntries));

        return response;
    }

    /**
     * 计算会计分录汇总统计
     */
    private JournalEntryQueryResponse.JournalEntrySummaryDto calculateJournalEntrySummary(List<JournalEntry> journalEntries) {
        JournalEntryQueryResponse.JournalEntrySummaryDto summary = new JournalEntryQueryResponse.JournalEntrySummaryDto();
        
        summary.setTotalEntries(journalEntries.size());
        
        // 统计不同类型的分录数量
        long paymentEntries = journalEntries.stream()
                .filter(entry -> EntryType.PAYMENT.equals(entry.getEntryType()))
                .count();
        long amortizationEntries = journalEntries.stream()
                .filter(entry -> EntryType.AMORTIZATION.equals(entry.getEntryType()))
                .count();
        
        summary.setPaymentEntries((int) paymentEntries);
        summary.setAmortizationEntries((int) amortizationEntries);

        // 计算总金额
        BigDecimal totalDebitAmount = journalEntries.stream()
                .map(JournalEntry::getTotalDr)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCreditAmount = journalEntries.stream()
                .map(JournalEntry::getTotalCr)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        summary.setTotalDebitAmount(totalDebitAmount);
        summary.setTotalCreditAmount(totalCreditAmount);
        summary.setBalanced(totalDebitAmount.compareTo(totalCreditAmount) == 0);

        // 计算日期范围
        if (!journalEntries.isEmpty()) {
            LocalDate earliestDate = journalEntries.stream()
                    .map(JournalEntry::getEntryDate)
                    .filter(date -> date != null)
                    .min(LocalDate::compareTo)
                    .orElse(null);
            
            LocalDate latestDate = journalEntries.stream()
                    .map(JournalEntry::getEntryDate)
                    .filter(date -> date != null)
                    .max(LocalDate::compareTo)
                    .orElse(null);
            
            summary.setEarliestEntryDate(earliestDate);
            summary.setLatestEntryDate(latestDate);
        }

        // 统计不同状态的分录数量
        long draftEntries = journalEntries.stream()
                .filter(entry -> EntryStatus.DRAFT.equals(entry.getStatus()))
                .count();
        long postedEntries = journalEntries.stream()
                .filter(entry -> EntryStatus.POSTED.equals(entry.getStatus()))
                .count();
        
        summary.setDraftEntries((int) draftEntries);
        summary.setPostedEntries((int) postedEntries);

        return summary;
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
