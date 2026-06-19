package com.microservices.propertyservice.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.microservices.propertyservice.dto.ApiError;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PropertyModelTest {

    @Test
    void propertyGettersAndSettersRoundTrip() {
        Property p = new Property();
        p.setId(1L);
        p.setTitle("Loft");
        p.setType("APARTMENT");
        p.setLocation("Bordeaux");
        p.setPrice(950.0);
        p.setDescription("Lumineux");
        p.setOwnerId(4L);
        p.getPhotoKeys().add("photo-1");

        assertThat(p.getId()).isEqualTo(1L);
        assertThat(p.getTitle()).isEqualTo("Loft");
        assertThat(p.getType()).isEqualTo("APARTMENT");
        assertThat(p.getLocation()).isEqualTo("Bordeaux");
        assertThat(p.getPrice()).isEqualTo(950.0);
        assertThat(p.getDescription()).isEqualTo("Lumineux");
        assertThat(p.getOwnerId()).isEqualTo(4L);
        assertThat(p.getPhotoKeys()).containsExactly("photo-1");
    }

    @Test
    void reservationGettersAndSettersRoundTrip() {
        LocalDate start = LocalDate.of(2025, 5, 1);
        LocalDate end = LocalDate.of(2025, 5, 8);
        Reservation r = new Reservation();
        r.setId(2L);
        r.setPropertyId(1L);
        r.setTenantId(3L);
        r.setStartDate(start);
        r.setEndDate(end);
        r.setStatus("PENDING");
        r.setTenantName("Jane Doe");
        r.setTenantEmail("jane@example.com");
        r.setTenantPhone("0600000000");

        assertThat(r.getId()).isEqualTo(2L);
        assertThat(r.getPropertyId()).isEqualTo(1L);
        assertThat(r.getTenantId()).isEqualTo(3L);
        assertThat(r.getStartDate()).isEqualTo(start);
        assertThat(r.getEndDate()).isEqualTo(end);
        assertThat(r.getStatus()).isEqualTo("PENDING");
        assertThat(r.getTenantName()).isEqualTo("Jane Doe");
        assertThat(r.getTenantEmail()).isEqualTo("jane@example.com");
        assertThat(r.getTenantPhone()).isEqualTo("0600000000");
    }

    @Test
    void apiErrorCarriesFields() {
        ApiError error = new ApiError(409, "Conflict", "déjà réservé", "property-service");

        assertThat(error.getStatus()).isEqualTo(409);
        assertThat(error.getError()).isEqualTo("Conflict");
        assertThat(error.getMessage()).isEqualTo("déjà réservé");
        assertThat(error.getService()).isEqualTo("property-service");
        assertThat(error.getTimestamp()).isNotNull();
    }
}
