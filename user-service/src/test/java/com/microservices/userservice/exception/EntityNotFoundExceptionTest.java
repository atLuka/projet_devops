package com.microservices.userservice.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EntityNotFoundExceptionTest {

    @Test
    void carriesMessageAndIsRuntimeException() {
        EntityNotFoundException ex = new EntityNotFoundException("user 1 not found");

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("user 1 not found");
    }
}
