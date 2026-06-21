package com.microservices.userservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.microservices.userservice.dto.ApiError;
import com.microservices.userservice.exception.EntityNotFoundException;
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
        ReflectionTestUtils.setField(handler, "serviceName", "user-service");
    }

    @Test
    void handleEntityNotFound_returns404() {
        ResponseEntity<ApiError> response = handler.handleEntityNotFound(
            new EntityNotFoundException("user 1 not found")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("user 1 not found");
        assertThat(response.getBody().getService()).isEqualTo("user-service");
    }

    @Test
    void handleIllegalArgument_returns400() {
        ResponseEntity<ApiError> response = handler.handleIllegalArgument(
            new IllegalArgumentException("invalid role")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("invalid role");
    }

    @Test
    void handleGenericException_returns500WithMaskedMessage() {
        ResponseEntity<ApiError> response = handler.handleGenericException(
            new RuntimeException("internal detail")
        );

        assertThat(response.getStatusCode()).isEqualTo(
            HttpStatus.INTERNAL_SERVER_ERROR
        );
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo(
            "Erreur interne du serveur"
        );
    }
}
