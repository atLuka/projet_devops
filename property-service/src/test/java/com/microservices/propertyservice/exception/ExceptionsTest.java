package com.microservices.propertyservice.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExceptionsTest {

    @Test
    void entityNotFound_carriesMessage() {
        EntityNotFoundException ex = new EntityNotFoundException("not found");
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("not found");
    }

    @Test
    void conflict_carriesMessage() {
        ConflictException ex = new ConflictException("conflict");
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("conflict");
    }

    @Test
    void serviceUnavailable_carriesMessage() {
        ServiceUnavailableException ex = new ServiceUnavailableException("down");
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("down");
    }
}
