package com.windsurf.contractengine.controller;

import com.windsurf.contractengine.dto.*;
import com.windsurf.contractengine.entity.Contract;
import com.windsurf.contractengine.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 合同管理控制器
 */
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "合同管理", description = "合同上传、查询、更新等操作")
public class ContractController {

    private final ContractService contractService;

    /**
     * 上传合同文档
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传合同文档", description = "上传合同文档并进行初步解析")
    public ResponseEntity<ContractResponse> uploadContract(
            @Parameter(description = "合同文档文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "合同基本信息")
            @Valid @ModelAttribute ContractCreateRequest request) {
        
        log.info("上传合同文档: {}", file.getOriginalFilename());
        ContractResponse response = contractService.uploadContract(file, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 查询已上传合同列表 (API 1.2)
     */
    @GetMapping
    @Operation(summary = "查询已上传合同列表", description = "分页查询已上传的合同列表，按上传时间倒序排列")
    public ResponseEntity<ApiResponse<ContractListResponse>> getUploadedContracts(
            @Parameter(description = "页码", example = "1")
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @Parameter(description = "合同状态", example = "COMPLETED")
            @RequestParam(required = false) String status,
            @Parameter(description = "开始日期", example = "2025-01-01")
            @RequestParam(name = "start_date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", example = "2025-12-31")
            @RequestParam(name = "end_date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        log.info("查询已上传合同列表: page={}, size={}, status={}, startDate={}, endDate={}", 
                page, size, status, startDate, endDate);
        
        // 验证分页参数
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        
        // 创建分页参数，按上传时间倒序排序
        Pageable pageable = PageRequest.of(
                page - 1, // Spring Data JPA的页码从0开始
                size,
                Sort.by(Sort.Direction.DESC, "uploadTime", "createdAt")
        );
        
        // 调用服务层查询
        ContractListResponse data = contractService.getUploadedContracts(
                status, startDate, endDate, pageable);
        
        // 生成traceId
        String traceId = "trace-" + UUID.randomUUID().toString().substring(0, 8);
        
        return ResponseEntity.ok(ApiResponse.success(data, traceId));
    }

    /**
     * 获取合同列表（旧接口，保留兼容性）
     */
    @GetMapping("/list")
    @Operation(summary = "获取合同列表", description = "分页查询合同列表")
    public ResponseEntity<PageResponse<ContractResponse>> getContracts(
            @Parameter(description = "合同类型过滤")
            @RequestParam(required = false) String contractType,
            @Parameter(description = "合同状态过滤")
            @RequestParam(required = false) String status,
            @Parameter(description = "搜索关键词")
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("查询合同列表: type={}, status={}, keyword={}", contractType, status, keyword);

        log.info("查询合同列表: type={}, status={}, keyword={}", contractType, status, keyword);
        PageResponse<ContractResponse> response = contractService.getContracts(
                contractType, status, keyword, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取合同详情 (API 1.3)
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询合同详情", description = "根据合同ID查询完整的合同详细信息，包含AI提取的结构化数据")
    public ResponseEntity<ApiResponse<ContractResponse>> getContractDetail(
            @Parameter(description = "合同ID", required = true)
            @PathVariable Long id) {

        log.info("查询合同详情: id={}", id);
        ContractResponse data = contractService.getContractById(id);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 更新合同信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新合同信息", description = "更新合同基本信息")
    public ResponseEntity<ContractResponse> updateContract(
            @Parameter(description = "合同ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "更新信息", required = true)
            @Valid @RequestBody ContractUpdateRequest request) {

        log.info("更新合同信息: id={}", id);
        ContractResponse response = contractService.updateContract(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除合同
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除合同", description = "删除指定合同及相关数据")
    public ResponseEntity<Void> deleteContract(
            @Parameter(description = "合同ID", required = true)
            @PathVariable Long id) {
        
        log.info("删除合同: id={}", id);
        contractService.deleteContract(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 执行字段提取
     */
    @PostMapping("/{id}/extract")
    @Operation(summary = "执行字段提取", description = "对合同文档执行字段提取分析")
    public ResponseEntity<Void> extractFields(
            @Parameter(description = "合同ID", required = true)
            @PathVariable Long id) {
        
        log.info("执行字段提取: id={}", id);
        contractService.extractFields(id);
        return ResponseEntity.accepted().build();
    }

    /**
     * 获取提取的字段
     */
    @GetMapping("/{id}/fields")
    @Operation(summary = "获取提取的字段", description = "获取合同字段提取结果")
    public ResponseEntity<?> getExtractedFields(
            @Parameter(description = "合同ID", required = true)
            @PathVariable Long id) {
        
        log.info("查询提取字段: id={}", id);
        var fields = contractService.getExtractedFields(id);
        return ResponseEntity.ok(fields);
    }

    /**
     * 生成支付计划
     */
    @PostMapping("/{id}/payment-schedule")
    @Operation(summary = "生成支付计划", description = "根据合同信息生成支付时间表")
    public ResponseEntity<Void> generatePaymentSchedule(
            @Parameter(description = "合同ID", required = true)
            @PathVariable Long id) {
        
        log.info("生成支付计划: id={}", id);
        contractService.generatePaymentSchedule(id);
        return ResponseEntity.accepted().build();
    }

    /**
     * 生成摊销计划
     */
    @PostMapping("/{id}/amortization-schedule")
    @Operation(summary = "生成摊销计划", description = "根据合同信息生成摊销时间表")
    public ResponseEntity<Void> generateAmortizationSchedule(
            @Parameter(description = "合同ID", required = true)
            @PathVariable Long id) {
        
        log.info("生成摊销计划: id={}", id);
        contractService.generateAmortizationSchedule(id);
        return ResponseEntity.accepted().build();
    }

    /**
     * 获取所有计划
     */
    @GetMapping("/{id}/schedules")
    @Operation(summary = "获取所有计划", description = "获取合同的支付计划和摊销计划")
    public ResponseEntity<?> getSchedules(
            @Parameter(description = "合同ID", required = true)
            @PathVariable Long id) {
        
        log.info("查询合同计划: id={}", id);
        var schedules = contractService.getSchedules(id);
        return ResponseEntity.ok(schedules);
    }

    /**
     * 生成会计分录
     */
    @PostMapping("/{id}/journal-entries")
    @Operation(summary = "生成会计分录", description = "根据支付和摊销计划生成会计分录")
    public ResponseEntity<Void> generateJournalEntries(
            @Parameter(description = "合同ID", required = true)
            @PathVariable Long id) {
        
        log.info("生成会计分录: id={}", id);
        contractService.generateJournalEntries(id);
        return ResponseEntity.accepted().build();
    }
}
