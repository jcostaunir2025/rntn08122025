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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Custom handler for Authentication errors (401)
 * Provides detailed error messages when authentication fails
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {

        log.warn("Authentication failed - Path: {} - Reason: {}",
                request.getRequestURI(),
                authException.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Authentication Required")
                .message(determineErrorMessage(authException, request))
                .details(Map.of(
                    "reason", authException.getMessage() != null ? authException.getMessage() : "Authentication failed",
                    "suggestion", determineSuggestion(request),
                    "path", request.getRequestURI()
                ))
                .path(request.getRequestURI())
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    /**
     * Determine appropriate error message based on the exception and request
     */
    private String determineErrorMessage(AuthenticationException exception, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isEmpty()) {
            return "Authentication required. Please provide a valid JWT token in the Authorization header.";
        }

        if (!authHeader.startsWith("Bearer ")) {
            return "Invalid Authorization header format. Please use: Bearer <token>";
        }

        // Token is present but invalid
        String message = exception.getMessage();
        if (message != null && message.toLowerCase().contains("expired")) {
            return "Your authentication token has expired. Please login again to obtain a new token.";
        }

        if (message != null && message.toLowerCase().contains("invalid")) {
            return "Invalid authentication token. Please login again to obtain a valid token.";
        }

        return "Authentication failed. Please verify your credentials and try again.";
    }

    /**
     * Provide helpful suggestion based on the request
     */
    private String determineSuggestion(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isEmpty()) {
            return "Include 'Authorization: Bearer <your-jwt-token>' header in your request. " +
                   "Obtain a token by calling POST /api/v1/auth/login";
        }

        if (!authHeader.startsWith("Bearer ")) {
            return "Ensure your Authorization header follows the format: 'Bearer <token>'";
        }

        return "Your token may be expired or invalid. Login again at POST /api/v1/auth/login";
    }
}

