package com.example.rntn.security;

import com.example.rntn.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Custom handler for Access Denied (403) errors
 * Provides detailed error messages when users lack permissions
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.warn("Access denied for user: {} - Path: {} - Reason: {}",
                getUsername(request),
                request.getRequestURI(),
                accessDeniedException.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Access Denied")
                .message("You don't have permission to access this resource. " +
                        extractPermissionHint(accessDeniedException.getMessage()))
                .details(Map.of(
                    "reason", "Insufficient permissions",
                    "suggestion", "Please contact your administrator to request the necessary permissions",
                    "path", request.getRequestURI(),
                    "method", request.getMethod()
                ))
                .path(request.getRequestURI())
                .build();

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    /**
     * Extract username from request
     */
    private String getUsername(HttpServletRequest request) {
        if (request.getUserPrincipal() != null) {
            return request.getUserPrincipal().getName();
        }
        return "anonymous";
    }

    /**
     * Try to extract permission requirement from exception message
     */
    private String extractPermissionHint(String message) {
        if (message == null || message.isEmpty()) {
            return "Check with your administrator for required permissions.";
        }

        // If message contains permission info, extract it
        if (message.contains("permission")) {
            return message;
        }

        return "Required permission: Check with your administrator.";
    }
}

