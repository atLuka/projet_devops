package com.microservices.messagingservice.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EntityNotFoundExceptionTest {

    @Test
    void carriesMessageAndIsRuntimeException() {
        EntityNotFoundException ex = new EntityNotFoundException("message 1 not found");

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("message 1 not found");
    }
}
