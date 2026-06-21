package com.microservices.propertyservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.microservices.propertyservice.dto.ApiError;
import com.microservices.propertyservice.exception.ConflictException;
import com.microservices.propertyservice.exception.EntityNotFoundException;
import com.microservices.propertyservice.exception.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        ReflectionTestUtils.setField(handler, "serviceName", "property-service");
    }

    @Test
    void handleEntityNotFound_returns404() {
        ResponseEntity<ApiError> response = handler.handleEntityNotFound(
            new EntityNotFoundException("property 1 not found")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("property 1 not found");
        assertThat(response.getBody().getService()).isEqualTo("property-service");
    }

    @Test
    void handleConflict_returns409() {
        ResponseEntity<ApiError> response = handler.handleConflict(
            new ConflictException("dates overlap")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).isEqualTo("dates overlap");
    }

    @Test
    void handleServiceUnavailable_returns503() {
        ResponseEntity<ApiError> response = handler.handleServiceUnavailable(
            new ServiceUnavailableException("minio down")
        );

        assertThat(response.getStatusCode()).isEqualTo(
            HttpStatus.SERVICE_UNAVAILABLE
        );
        assertThat(response.getBody().getStatus()).isEqualTo(503);
        assertThat(response.getBody().getError()).isEqualTo("Service Unavailable");
    }

    @Test
    void handleIllegalArgument_returns400() {
        ResponseEntity<ApiError> response = handler.handleIllegalArgument(
            new IllegalArgumentException("invalid date range")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("invalid date range");
    }

    @Test
    void handleGeneral_returns500() {
        ResponseEntity<ApiError> response = handler.handleGeneral(
            new RuntimeException("boom")
        );

        assertThat(response.getStatusCode()).isEqualTo(
            HttpStatus.INTERNAL_SERVER_ERROR
        );
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("boom");
    }
}
