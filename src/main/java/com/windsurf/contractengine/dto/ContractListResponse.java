package com.windsurf.contractengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 合同列表响应DTO
 * 用于API 1.2 查询已上传合同列表
 */
@Data
@Schema(description = "合同列表响应")
public class ContractListResponse {

    @Schema(description = "总记录数", example = "50")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Integer page;

    @Schema(description = "每页大小", example = "10")
    private Integer size;

    @Schema(description = "合同列表")
    private List<ContractListItemResponse> items;

    public static ContractListResponse of(List<ContractListItemResponse> items, int page, int size, long total) {
        ContractListResponse response = new ContractListResponse();
        response.setItems(items);
        response.setPage(page);
        response.setSize(size);
        response.setTotal(total);
        return response;
    }
}
