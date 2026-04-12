package com.microservices.messagingservice.controller;

import com.microservices.messagingservice.model.Message;
import com.microservices.messagingservice.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);
    private final MessageRepository messageRepository;

    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping("/reservation/{reservationId}")
    public List<Message> getByReservation(@PathVariable Long reservationId) {
        log.info("GET /messages/reservation/{}", reservationId);
        return messageRepository.findByReservationIdOrderBySentAtAsc(reservationId);
    }

    @PostMapping
    public Message sendMessage(@RequestBody Message message) {
        log.info("POST /messages - reservationId={}, sender={}", message.getReservationId(), message.getSenderRole());
        message.setSentAt(LocalDateTime.now());
        message.setRead(false);
        return messageRepository.save(message);
    }

    @GetMapping("/unread")
    public Map<String, Long> getUnreadCounts(@RequestParam String role, @RequestParam List<Long> reservationIds) {
        log.info("GET /messages/unread?role={}&reservationIds={}", role, reservationIds);
        Map<String, Long> counts = new java.util.HashMap<>();
        long total = 0;
        for (Long resId : reservationIds) {
            long count = messageRepository.countByReservationIdAndReadFalseAndSenderRoleNot(resId, role);
            if (count > 0) counts.put(String.valueOf(resId), count);
            total += count;
        }
        counts.put("total", total);
        return counts;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "messaging-service");
    }

    @PutMapping("/reservation/{reservationId}/read")
    public void markAsRead(@PathVariable Long reservationId, @RequestParam String role) {
        log.info("PUT /messages/reservation/{}/read?role={}", reservationId, role);
        String otherRole = "TENANT".equals(role) ? "OWNER" : "TENANT";
        List<Message> messages = messageRepository.findByReservationIdOrderBySentAtAsc(reservationId);
        messages.stream()
            .filter(m -> m.getSenderRole().equals(otherRole) && !m.isRead())
            .forEach(m -> {
                m.setRead(true);
                messageRepository.save(m);
            });
    }
}
