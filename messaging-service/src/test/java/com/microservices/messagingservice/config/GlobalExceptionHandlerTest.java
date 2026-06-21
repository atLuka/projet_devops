package com.microservices.messagingservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.microservices.messagingservice.dto.ApiError;
import com.microservices.messagingservice.exception.EntityNotFoundException;
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
        ReflectionTestUtils.setField(handler, "serviceName", "messaging-service");
    }

    @Test
    void handleEntityNotFound_returns404() {
        ResponseEntity<ApiError> response = handler.handleEntityNotFound(
            new EntityNotFoundException("message not found")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("message not found");
        assertThat(response.getBody().getService()).isEqualTo("messaging-service");
    }

    @Test
    void handleIllegalArgument_returns400() {
        ResponseEntity<ApiError> response = handler.handleIllegalArgument(
            new IllegalArgumentException("empty content")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("empty content");
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
