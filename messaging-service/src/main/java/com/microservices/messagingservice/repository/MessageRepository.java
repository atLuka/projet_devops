package com.microservices.messagingservice.repository;

import com.microservices.messagingservice.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByReservationIdOrderBySentAtAsc(Long reservationId);

    long countByReservationIdAndReadFalseAndSenderRoleNot(Long reservationId, String senderRole);
}
