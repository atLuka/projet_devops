package com.microservices.userservice.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.microservices.userservice.dto.ApiError;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void gettersAndSettersRoundTrip() {
        User user = new User();
        user.setId(7L);
        user.setEmail("jane@example.com");
        user.setPasswordHash("secret");
        user.setRole("OWNER");
        user.setFirstName("Jane");
        user.setLastName("Roe");
        user.setPhone("0102030405");

        assertThat(user.getId()).isEqualTo(7L);
        assertThat(user.getEmail()).isEqualTo("jane@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("secret");
        assertThat(user.getRole()).isEqualTo("OWNER");
        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Roe");
        assertThat(user.getPhone()).isEqualTo("0102030405");
    }

    @Test
    void apiErrorCarriesFieldsAndTimestamp() {
        ApiError error = new ApiError(404, "Not Found", "missing", "user-service");

        assertThat(error.getStatus()).isEqualTo(404);
        assertThat(error.getError()).isEqualTo("Not Found");
        assertThat(error.getMessage()).isEqualTo("missing");
        assertThat(error.getService()).isEqualTo("user-service");
        assertThat(error.getTimestamp()).isNotBlank();
    }
}
