package com.microservices.propertyservice.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    void gettersAndSettersRoundTrip() {
        Reservation r = new Reservation();
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 10);

        r.setId(5L);
        r.setPropertyId(2L);
        r.setTenantId(9L);
        r.setStartDate(start);
        r.setEndDate(end);
        r.setStatus("CONFIRMED");
        r.setTenantName("John Doe");
        r.setTenantEmail("john@example.com");
        r.setTenantPhone("0600000000");

        assertThat(r.getId()).isEqualTo(5L);
        assertThat(r.getPropertyId()).isEqualTo(2L);
        assertThat(r.getTenantId()).isEqualTo(9L);
        assertThat(r.getStartDate()).isEqualTo(start);
        assertThat(r.getEndDate()).isEqualTo(end);
        assertThat(r.getStatus()).isEqualTo("CONFIRMED");
        assertThat(r.getTenantName()).isEqualTo("John Doe");
        assertThat(r.getTenantEmail()).isEqualTo("john@example.com");
        assertThat(r.getTenantPhone()).isEqualTo("0600000000");
    }

    @Test
    void newReservationHasNullFields() {
        Reservation r = new Reservation();
        assertThat(r.getId()).isNull();
        assertThat(r.getStatus()).isNull();
    }
}
