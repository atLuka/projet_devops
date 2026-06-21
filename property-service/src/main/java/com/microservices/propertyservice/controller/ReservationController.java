package com.microservices.propertyservice.controller;

import com.microservices.propertyservice.exception.ConflictException;
import com.microservices.propertyservice.exception.EntityNotFoundException;
import com.microservices.propertyservice.model.Property;
import com.microservices.propertyservice.model.Reservation;
import com.microservices.propertyservice.repository.PropertyRepository;
import com.microservices.propertyservice.repository.ReservationRepository;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(
        ReservationController.class
    );

    private final ReservationRepository reservationRepository;
    private final PropertyRepository propertyRepository;

    public ReservationController(
        ReservationRepository reservationRepository,
        PropertyRepository propertyRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.propertyRepository = propertyRepository;
    }

    // Liaison directe de l'entité acceptée : CRUD interne simple.
    @SuppressWarnings("java:S4684")
    @PostMapping
    public Reservation createReservation(@RequestBody Reservation reservation) {
        log.info(
            "POST /reservations - propertyId={}, tenantId={}",
            reservation.getPropertyId(),
            reservation.getTenantId()
        );
        List<Reservation> overlapping = reservationRepository.findOverlapping(
            reservation.getPropertyId(),
            reservation.getStartDate(),
            reservation.getEndDate()
        );

        if (!overlapping.isEmpty()) {
            throw new ConflictException(
                "Ces dates sont déjà réservées pour ce logement"
            );
        }

        reservation.setStatus("PENDING");
        return reservationRepository.save(reservation);
    }

    @GetMapping("/owner/{ownerId}")
    public List<Reservation> getByOwner(@PathVariable Long ownerId) {
        log.info("GET /reservations/owner/{}", ownerId);
        List<Long> propertyIds = propertyRepository
            .findByOwnerId(ownerId)
            .stream()
            .map(Property::getId)
            .toList();
        return reservationRepository
            .findAll()
            .stream()
            .filter(r -> propertyIds.contains(r.getPropertyId()))
            .toList();
    }

    @GetMapping("/tenant/{tenantId}")
    public List<Reservation> getByTenant(@PathVariable Long tenantId) {
        log.info("GET /reservations/tenant/{}", tenantId);
        return reservationRepository.findByTenantId(tenantId);
    }

    @GetMapping("/property/{propertyId}")
    public List<Reservation> getByProperty(@PathVariable Long propertyId) {
        log.info("GET /reservations/property/{}", propertyId);
        return reservationRepository.findByPropertyId(propertyId);
    }

    @PutMapping("/{id}/status")
    public Reservation updateStatus(
        @PathVariable Long id,
        @RequestBody Map<String, String> body
    ) {
        log.info(
            "PUT /reservations/{}/status - status={}",
            id,
            sanitize(body.get("status"))
        );
        Reservation reservation = reservationRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Réservation non trouvée : " + id)
            );
        reservation.setStatus(body.get("status"));
        return reservationRepository.save(reservation);
    }

    // Neutralise les retours chariot/sauts de ligne pour éviter l'injection de logs (CRLF).
    private static String sanitize(Object value) {
        return value == null ? "null" : String.valueOf(value).replaceAll("[\\r\\n]", "_");
    }
}
