package com.example.rntn.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response structure for all API errors
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Timestamp when the error occurred
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code (e.g., 400, 403, 404, 500)
     */
    private int status;

    /**
     * Short error title (e.g., "Validation Failed", "Access Denied")
     */
    private String error;

    /**
     * Detailed error message explaining what went wrong
     */
    private String message;

    /**
     * Additional details about the error (optional)
     * Can include field-level validation errors, suggestions, etc.
     */
    private Map<String, Object> details;

    /**
     * Request path that caused the error
     */
    private String path;
}

