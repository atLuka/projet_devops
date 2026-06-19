package com.microservices.propertyservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.microservices.propertyservice.model.Reservation;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:reservationdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    }
)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    private Reservation reservation(
        long propertyId,
        String status,
        LocalDate start,
        LocalDate end
    ) {
        Reservation r = new Reservation();
        r.setPropertyId(propertyId);
        r.setTenantId(5L);
        r.setStatus(status);
        r.setStartDate(start);
        r.setEndDate(end);
        return r;
    }

    @Test
    void findOverlapping_detectsDateClash() {
        reservationRepository.save(
            reservation(
                1L,
                "CONFIRMED",
                LocalDate.of(2025, 6, 10),
                LocalDate.of(2025, 6, 20)
            )
        );

        var overlap = reservationRepository.findOverlapping(
            1L,
            LocalDate.of(2025, 6, 15),
            LocalDate.of(2025, 6, 25)
        );

        assertThat(overlap).hasSize(1);
    }

    @Test
    void findOverlapping_ignoresCancelledAndDisjointDates() {
        reservationRepository.save(
            reservation(
                1L,
                "CANCELLED",
                LocalDate.of(2025, 6, 10),
                LocalDate.of(2025, 6, 20)
            )
        );
        reservationRepository.save(
            reservation(
                1L,
                "CONFIRMED",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 5)
            )
        );

        var overlap = reservationRepository.findOverlapping(
            1L,
            LocalDate.of(2025, 6, 15),
            LocalDate.of(2025, 6, 25)
        );

        assertThat(overlap).isEmpty();
    }

    @Test
    void findByPropertyIdAndStatusNot_excludesGivenStatus() {
        reservationRepository.save(
            reservation(
                1L,
                "CANCELLED",
                LocalDate.of(2025, 6, 10),
                LocalDate.of(2025, 6, 20)
            )
        );
        reservationRepository.save(
            reservation(
                1L,
                "CONFIRMED",
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 7, 5)
            )
        );

        var active = reservationRepository.findByPropertyIdAndStatusNot(
            1L,
            "CANCELLED"
        );

        assertThat(active).hasSize(1);
        assertThat(active.get(0).getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void findByTenantId_returnsTenantReservations() {
        reservationRepository.save(
            reservation(
                1L,
                "PENDING",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 9, 3)
            )
        );

        assertThat(reservationRepository.findByTenantId(5L)).hasSize(1);
        assertThat(reservationRepository.findByTenantId(999L)).isEmpty();
    }
}
