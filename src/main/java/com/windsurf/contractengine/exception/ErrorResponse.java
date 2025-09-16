package com.windsurf.contractengine.exception;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 错误响应类
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * HTTP状态码
     */
    private int status;

    /**
     * 错误类型
     */
    private String error;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 详细错误信息
     */
    private Map<String, Object> details;
}
