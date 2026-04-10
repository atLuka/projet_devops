package com.microservices.propertyservice.config;

import com.microservices.propertyservice.dto.ApiError;
import com.microservices.propertyservice.exception.ConflictException;
import com.microservices.propertyservice.exception.EntityNotFoundException;
import com.microservices.propertyservice.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(
        GlobalExceptionHandler.class
    );

    @Value("${spring.application.name}")
    private String serviceName;

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFound(
        EntityNotFoundException ex
    ) {
        log.warn("Entity not found: {}", ex.getMessage());
        ApiError error = new ApiError(
            404,
            "Not Found",
            ex.getMessage(),
            serviceName
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        ApiError error = new ApiError(
            409,
            "Conflict",
            ex.getMessage(),
            serviceName
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ApiError> handleServiceUnavailable(
        ServiceUnavailableException ex
    ) {
        log.error("Service unavailable: {}", ex.getMessage());
        ApiError error = new ApiError(
            503,
            "Service Unavailable",
            ex.getMessage(),
            serviceName
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            error
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
        IllegalArgumentException ex
    ) {
        log.warn("Bad request: {}", ex.getMessage());
        ApiError error = new ApiError(
            400,
            "Bad Request",
            ex.getMessage(),
            serviceName
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ApiError error = new ApiError(
            500,
            "Internal Server Error",
            ex.getMessage(),
            serviceName
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            error
        );
    }
}
