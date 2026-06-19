package com.microservices.messagingservice.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.microservices.messagingservice.dto.ApiError;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class MessageTest {

    @Test
    void gettersAndSettersRoundTrip() {
        LocalDateTime sentAt = LocalDateTime.of(2025, 6, 1, 12, 0);
        Message m = new Message();
        m.setId(3L);
        m.setReservationId(1L);
        m.setSenderId(5L);
        m.setSenderRole("TENANT");
        m.setContent("Bonjour");
        m.setSentAt(sentAt);
        m.setRead(true);

        assertThat(m.getId()).isEqualTo(3L);
        assertThat(m.getReservationId()).isEqualTo(1L);
        assertThat(m.getSenderId()).isEqualTo(5L);
        assertThat(m.getSenderRole()).isEqualTo("TENANT");
        assertThat(m.getContent()).isEqualTo("Bonjour");
        assertThat(m.getSentAt()).isEqualTo(sentAt);
        assertThat(m.isRead()).isTrue();
    }

    @Test
    void apiErrorCarriesFields() {
        ApiError error = new ApiError(400, "Bad Request", "invalid", "messaging-service");

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getError()).isEqualTo("Bad Request");
        assertThat(error.getMessage()).isEqualTo("invalid");
        assertThat(error.getService()).isEqualTo("messaging-service");
        assertThat(error.getTimestamp()).isNotNull();
    }
}
