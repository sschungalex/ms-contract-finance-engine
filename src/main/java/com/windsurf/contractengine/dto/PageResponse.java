package com.windsurf.contractengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分页响应DTO
 */
@Data
@Schema(description = "分页响应")
public class PageResponse<T> {

    @Schema(description = "数据列表")
    private List<T> content;

    @Schema(description = "当前页码", example = "0")
    private int page;

    @Schema(description = "每页大小", example = "20")
    private int size;

    @Schema(description = "总元素数", example = "100")
    private long totalElements;

    @Schema(description = "总页数", example = "5")
    private int totalPages;

    @Schema(description = "是否为第一页", example = "true")
    private boolean first;

    @Schema(description = "是否为最后一页", example = "false")
    private boolean last;

    @Schema(description = "是否有下一页", example = "true")
    private boolean hasNext;

    @Schema(description = "是否有上一页", example = "false")
    private boolean hasPrevious;

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(totalElements);
        response.setTotalPages((int) Math.ceil((double) totalElements / size));
        response.setFirst(page == 0);
        response.setLast(page >= response.getTotalPages() - 1);
        response.setHasNext(page < response.getTotalPages() - 1);
        response.setHasPrevious(page > 0);
        return response;
    }
}
