package com.microservices.messagingservice;

import com.microservices.messagingservice.model.Message;
import com.microservices.messagingservice.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final MessageRepository messageRepository;

    public DataSeeder(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void run(String... args) {
        if (messageRepository.count() > 0) {
            log.info("Messages already seeded, skipping.");
            return;
        }

        log.info("Seeding messages...");

        // Reservation 1: property 1 (owner 1), tenant 5
        seedMessage(1L, 5L, "TENANT",
            "Bonjour, j'ai bien recu la confirmation. A quelle heure puis-je arriver ?",
            LocalDateTime.now().minusHours(48));
        seedMessage(1L, 1L, "OWNER",
            "Bonjour ! Vous pouvez arriver a partir de 15h. Le code de la porte est 4521.",
            LocalDateTime.now().minusHours(47));
        seedMessage(1L, 5L, "TENANT",
            "Parfait, merci ! Y a-t-il un parking a proximite ?",
            LocalDateTime.now().minusHours(46));
        seedMessage(1L, 1L, "OWNER",
            "Oui, il y a un parking gratuit juste en face de l'immeuble.",
            LocalDateTime.now().minusHours(45));

        // Reservation 3: property 31 (owner 1), tenant 5
        seedMessage(3L, 5L, "TENANT",
            "Bonjour, est-ce que le linge de lit est fourni ?",
            LocalDateTime.now().minusHours(72));
        seedMessage(3L, 1L, "OWNER",
            "Bonjour, oui bien sur ! Draps, serviettes et torchons sont fournis.",
            LocalDateTime.now().minusHours(71));

        // Reservation 6: property 2 (owner 1), tenant 6
        seedMessage(6L, 6L, "TENANT",
            "Bonjour, nous arriverons vers 18h. Est-ce que cela vous convient ?",
            LocalDateTime.now().minusHours(24));
        seedMessage(6L, 1L, "OWNER",
            "Pas de souci, je vous laisse les cles dans la boite a cles. Le code est 7890.",
            LocalDateTime.now().minusHours(23));
        seedMessage(6L, 6L, "TENANT",
            "Super, merci beaucoup !",
            LocalDateTime.now().minusHours(22));

        // Reservation 11: property 3 (owner 1), tenant 7
        seedMessage(11L, 7L, "TENANT",
            "Bonjour, le wifi est-il inclus dans le logement ?",
            LocalDateTime.now().minusHours(36));
        seedMessage(11L, 1L, "OWNER",
            "Oui, le wifi est inclus. Le mot de passe est sur le frigo.",
            LocalDateTime.now().minusHours(35));

        // Reservation 21: property 5 (owner 2), tenant 9
        seedMessage(21L, 9L, "TENANT",
            "Bonjour, est-il possible d'arriver un peu plus tot, vers 13h ?",
            LocalDateTime.now().minusHours(12));
        seedMessage(21L, 2L, "OWNER",
            "Bonjour ! Malheureusement ce n'est pas possible, le menage est prevu jusqu'a 14h30.",
            LocalDateTime.now().minusHours(11));
        seedMessage(21L, 9L, "TENANT",
            "D'accord, je comprends. On arrivera vers 15h alors.",
            LocalDateTime.now().minusHours(10));

        // Reservation 27: property 7 (owner 2), tenant 11
        seedMessage(27L, 11L, "TENANT",
            "Bonjour, les animaux sont-ils acceptes ? J'ai un petit chien.",
            LocalDateTime.now().minusHours(96));
        seedMessage(27L, 2L, "OWNER",
            "Bonjour, oui les petits animaux sont acceptes. Prevoyez juste une couverture pour le canape.",
            LocalDateTime.now().minusHours(95));
        seedMessage(27L, 11L, "TENANT",
            "Merci, c'est note ! A bientot.",
            LocalDateTime.now().minusHours(94));

        // Reservation 30: property 10 (owner 3), tenant 13
        seedMessage(30L, 13L, "TENANT",
            "Bonjour, ou puis-je recuperer les cles ?",
            LocalDateTime.now().minusHours(6));
        seedMessage(30L, 3L, "OWNER",
            "Bonjour ! Les cles sont dans la boite a lettres, code 1234. N'hesitez pas si vous avez des questions.",
            LocalDateTime.now().minusHours(5));

        log.info("Messages seeded successfully.");
    }

    private void seedMessage(Long reservationId, Long senderId, String senderRole, String content, LocalDateTime sentAt) {
        Message message = new Message();
        message.setReservationId(reservationId);
        message.setSenderId(senderId);
        message.setSenderRole(senderRole);
        message.setContent(content);
        message.setSentAt(sentAt);
        message.setRead(true);
        messageRepository.save(message);
    }
}
