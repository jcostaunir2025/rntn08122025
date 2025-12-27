package com.example.rntn.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom exception for business logic violations
 */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String error;
    private final Map<String, Object> details;

    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.error = "Business Rule Violation";
        this.details = new HashMap<>();
    }

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.error = "Business Rule Violation";
        this.details = new HashMap<>();
    }

    public BusinessException(String message, String error, HttpStatus status) {
        super(message);
        this.status = status;
        this.error = error;
        this.details = new HashMap<>();
    }

    public BusinessException(String message, Map<String, Object> details) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.error = "Business Rule Violation";
        this.details = details != null ? details : new HashMap<>();
    }

    public BusinessException(String message, String error, HttpStatus status, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.error = error;
        this.details = details != null ? details : new HashMap<>();
    }
}

