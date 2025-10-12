package com.windsurf.contractengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一API响应DTO
 */
@Data
@Schema(description = "统一API响应")
public class ApiResponse<T> {

    @Schema(description = "响应码，0表示成功，非0表示失败", example = "0")
    private Integer code;

    @Schema(description = "响应消息", example = "success")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "链路追踪ID", example = "trace-123456")
    private String traceId;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setMessage("success");
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> success(T data, String traceId) {
        ApiResponse<T> response = success(data);
        response.setTraceId(traceId);
        return response;
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    public static <T> ApiResponse<T> error(Integer code, String message, String traceId) {
        ApiResponse<T> response = error(code, message);
        response.setTraceId(traceId);
        return response;
    }
}
