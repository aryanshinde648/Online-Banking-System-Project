package com.obs.Online_Banking_System.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.warn("RESOURCE_NOT_FOUND: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

     /**
     * Handle UserAlreadyExistsException
     */
    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExistsException(CustomerAlreadyExistsException ex, WebRequest request) {
        log.warn("USER_ALREADY_EXISTS: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Handle UnauthorizedAccessException
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Object> handleUnauthorizedAccessException(UnauthorizedAccessException ex, WebRequest request) {
        log.warn("UNAUTHORIZED_ACCESS: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    /**
     * Handle general exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.",
                request
        );
    }

    /**
     * Create consistent error response structure
     */
    private ResponseEntity<Object> createErrorResponse(HttpStatus status, String message, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, status);
    }
    
}
