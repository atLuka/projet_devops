package com.microservices.messagingservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.microservices.messagingservice.model.Message;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:messagingdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    }
)
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    private Message message(
        Long reservationId,
        String role,
        boolean read,
        LocalDateTime sentAt
    ) {
        Message m = new Message();
        m.setReservationId(reservationId);
        m.setSenderId(1L);
        m.setSenderRole(role);
        m.setContent("hello");
        m.setSentAt(sentAt);
        m.setRead(read);
        return m;
    }

    @Test
    void findByReservation_returnsChronologicalOrder() {
        LocalDateTime base = LocalDateTime.of(2025, 1, 1, 10, 0);
        messageRepository.save(message(1L, "OWNER", true, base.plusMinutes(5)));
        messageRepository.save(message(1L, "TENANT", true, base));
        messageRepository.save(message(2L, "TENANT", true, base));

        var messages = messageRepository.findByReservationIdOrderBySentAtAsc(1L);

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getSenderRole()).isEqualTo("TENANT");
        assertThat(messages.get(1).getSenderRole()).isEqualTo("OWNER");
    }

    @Test
    void countsUnreadFromOtherRoleOnly() {
        LocalDateTime now = LocalDateTime.now();
        messageRepository.save(message(1L, "OWNER", false, now)); // counts for TENANT
        messageRepository.save(message(1L, "OWNER", true, now)); // already read
        messageRepository.save(message(1L, "TENANT", false, now)); // same role, excluded

        long unreadForTenant =
            messageRepository.countByReservationIdAndReadFalseAndSenderRoleNot(
                1L,
                "TENANT"
            );

        assertThat(unreadForTenant).isEqualTo(1L);
    }
}
