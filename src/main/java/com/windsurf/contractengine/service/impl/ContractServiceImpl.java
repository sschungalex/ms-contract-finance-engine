package com.windsurf.contractengine.service.impl;

import com.windsurf.contractengine.dto.*;
import com.windsurf.contractengine.entity.Contract;
import com.windsurf.contractengine.enums.AIProcessingStatus;
import com.windsurf.contractengine.enums.ContractStatus;
import com.windsurf.contractengine.exception.ResourceNotFoundException;
import com.windsurf.contractengine.repository.ContractRepository;
import com.windsurf.contractengine.service.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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

    @Override
    @Transactional
    public ContractResponse uploadContract(MultipartFile file, ContractCreateRequest request) {
        log.info("开始上传合同文件: {}", file.getOriginalFilename());

        // TODO: 实现文件上传和AI处理逻辑
        throw new UnsupportedOperationException("上传合同功能待实现");
    }

    @Override
    @Transactional(readOnly = true)
    public ContractListResponse getUploadedContracts(String status, LocalDate startDate,
                                                     LocalDate endDate, Pageable pageable) {
        log.info("查询已上传合同列表: status={}, startDate={}, endDate={}, page={}, size={}",
                status, startDate, endDate, pageable.getPageNumber(), pageable.getPageSize());

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

        return ContractListResponse.of(
                items,
                pageable.getPageNumber() + 1, // API使用1-based页码
                pageable.getPageSize(),
                contractPage.getTotalElements()
        );
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
        log.info("查询合同详情: id={}", id);

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("合同不存在: " + id));

        return convertToResponse(contract);
    }

    @Override
    @Transactional
    public ContractResponse updateContract(Long id, ContractUpdateRequest request) {
        log.info("更新合同信息: id={}", id);

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("合同不存在: " + id));

        // TODO: 实现合同更新逻辑
        throw new UnsupportedOperationException("更新合同功能待实现");
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
    @Transactional
    public void generateAmortizationSchedule(Long id) {
        log.info("生成摊销计划: id={}", id);

        // TODO: 实现生成摊销计划逻辑
        throw new UnsupportedOperationException("生成摊销计划功能待实现");
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
     */
    private ContractResponse convertToResponse(Contract contract) {
        ContractResponse response = new ContractResponse();
        response.setId(contract.getId());
        response.setContractNumber(contract.getContractNumber());
        response.setContractName(contract.getContractName());
        response.setContractType(contract.getContractType());
        response.setCounterparty(contract.getCounterparty());
        response.setStartDate(contract.getStartDate());
        response.setEndDate(contract.getEndDate());
        response.setStatus(contract.getStatus());
        response.setTotalAmount(contract.getTotalAmount());
        response.setCurrency(contract.getCurrency());
        response.setPaymentFrequency(contract.getPaymentFrequency());
        response.setPaymentMethod(contract.getPaymentMethod());
        response.setOriginalFilename(contract.getOriginalFilename());
        response.setFileSize(contract.getFileSize());
        response.setCreatedAt(contract.getCreatedAt());
        response.setUpdatedAt(contract.getUpdatedAt());
        response.setCreatedBy(contract.getCreatedBy());
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
}
