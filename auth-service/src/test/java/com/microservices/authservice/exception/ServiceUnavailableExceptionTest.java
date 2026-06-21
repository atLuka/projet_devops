package com.microservices.authservice.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ServiceUnavailableExceptionTest {

    @Test
    void carriesMessageAndIsRuntimeException() {
        ServiceUnavailableException ex = new ServiceUnavailableException(
            "user-service unreachable"
        );

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("user-service unreachable");
    }
}
