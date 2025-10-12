package com.windsurf.contractengine.service;

import com.windsurf.contractengine.dto.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * 合同服务接口
 */
public interface ContractService {

    /**
     * 上传合同文档
     * 
     * @param file 合同文档文件
     * @param request 合同基本信息
     * @return 合同响应
     */
    ContractResponse uploadContract(MultipartFile file, ContractCreateRequest request);

    /**
     * 查询已上传合同列表 (API 1.2)
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param status 合同状态过滤
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 包含合同列表的API响应
     */
    ApiResponse<ContractListResponse> getUploadedContracts(Integer page, Integer size,
                                                           String status, LocalDate startDate, 
                                                           LocalDate endDate);

    /**
     * 分页查询合同列表
     * 
     * @param contractType 合同类型过滤
     * @param status 合同状态过滤
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 分页合同列表
     */
    PageResponse<ContractResponse> getContracts(String contractType, String status, 
                                               String keyword, Pageable pageable);

    /**
     * 根据ID获取合同详情 (API 1.3)
     * 包含AI提取的结构化数据，用于编辑表单回显
     * 
     * @param id 合同ID
     * @return 合同完整详情
     */
    ContractResponse getContractById(Long id);

    /**
     * 更新合同信息 (API 1.4)
     * 
     * @param id 合同ID
     * @param request 更新请求
     * @return 更新响应（包含更新字段列表）
     */
    ContractUpdateResponse updateContract(Long id, ContractUpdateRequest request);

    /**
     * 删除合同
     * 
     * @param id 合同ID
     */
    void deleteContract(Long id);

    /**
     * 执行字段提取
     * 
     * @param id 合同ID
     */
    void extractFields(Long id);

    /**
     * 获取提取的字段
     * 
     * @param id 合同ID
     * @return 提取的字段列表
     */
    Object getExtractedFields(Long id);

    /**
     * 生成支付计划
     * 
     * @param id 合同ID
     */
    void generatePaymentSchedule(Long id);

    /**
     * 生成并获取摊销计划 (API 1.5)
     * 根据合同信息生成并返回预付摊销表
     * 
     * @param id 合同ID
     * @return 摊销计划响应
     */
    AmortizationScheduleResponse generateAmortizationSchedule(Long id);

    /**
     * 获取所有计划
     * 
     * @param id 合同ID
     * @return 计划信息
     */
    Object getSchedules(Long id);

    /**
     * 生成会计分录
     * 
     * @param id 合同ID
     */
    void generateJournalEntries(Long id);
}
