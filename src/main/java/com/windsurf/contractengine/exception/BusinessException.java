package com.windsurf.contractengine.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String path;

    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.path = null;
    }

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.path = null;
    }

    public BusinessException(String message, HttpStatus status, String path) {
        super(message);
        this.status = status;
        this.path = path;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.BAD_REQUEST;
        this.path = null;
    }

    public BusinessException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
        this.path = null;
    }
}
