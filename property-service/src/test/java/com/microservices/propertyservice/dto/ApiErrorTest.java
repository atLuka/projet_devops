package com.microservices.propertyservice.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiErrorTest {

    @Test
    void constructorPopulatesFieldsAndTimestamp() {
        ApiError error = new ApiError(409, "Conflict", "overlap", "property-service");

        assertThat(error.getStatus()).isEqualTo(409);
        assertThat(error.getError()).isEqualTo("Conflict");
        assertThat(error.getMessage()).isEqualTo("overlap");
        assertThat(error.getService()).isEqualTo("property-service");
        assertThat(error.getTimestamp()).isNotNull();
    }

    @Test
    void settersUpdateFields() {
        ApiError error = new ApiError(0, null, null, null);
        error.setStatus(404);
        error.setError("Not Found");
        error.setMessage("missing");
        error.setService("svc");
        error.setTimestamp("2026-01-01T00:00");

        assertThat(error.getStatus()).isEqualTo(404);
        assertThat(error.getError()).isEqualTo("Not Found");
        assertThat(error.getMessage()).isEqualTo("missing");
        assertThat(error.getService()).isEqualTo("svc");
        assertThat(error.getTimestamp()).isEqualTo("2026-01-01T00:00");
    }
}
