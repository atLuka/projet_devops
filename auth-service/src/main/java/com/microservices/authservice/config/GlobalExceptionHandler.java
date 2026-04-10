package com.microservices.authservice.config;

import com.microservices.authservice.dto.ApiError;
import com.microservices.authservice.exception.ServiceUnavailableException;
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

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ApiError> handleServiceUnavailable(
        ServiceUnavailableException ex
    ) {
        log.error("Service indisponible: {}", ex.getMessage());
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
        log.warn("Requete invalide: {}", ex.getMessage());
        ApiError error = new ApiError(
            400,
            "Bad Request",
            ex.getMessage(),
            serviceName
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        log.error("Erreur inattendue: {}", ex.getMessage(), ex);
        ApiError error = new ApiError(
            500,
            "Internal Server Error",
            "Une erreur interne est survenue",
            serviceName
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            error
        );
    }
}
