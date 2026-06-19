package com.microservices.propertyservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.microservices.propertyservice.model.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:propertydb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    }
)
class PropertyRepositoryTest {

    @Autowired
    private PropertyRepository propertyRepository;

    private Property property(String type, String location, double price, long ownerId) {
        Property p = new Property();
        p.setTitle("Logement " + location);
        p.setType(type);
        p.setLocation(location);
        p.setPrice(price);
        p.setOwnerId(ownerId);
        return p;
    }

    @BeforeEach
    void seed() {
        propertyRepository.save(property("APARTMENT", "Paris", 800.0, 1L));
        propertyRepository.save(property("HOUSE", "Lyon", 1500.0, 1L));
        propertyRepository.save(property("APARTMENT", "Marseille", 600.0, 2L));
    }

    @Test
    void findByOwnerId_returnsOnlyOwnerProperties() {
        assertThat(propertyRepository.findByOwnerId(1L)).hasSize(2);
        assertThat(propertyRepository.findByOwnerId(2L)).hasSize(1);
    }

    @Test
    void searchProperties_filtersByType() {
        var results = propertyRepository.searchProperties("APARTMENT", null, null);

        assertThat(results)
            .hasSize(2)
            .allMatch(p -> p.getType().equals("APARTMENT"));
    }

    @Test
    void searchProperties_filtersByLocationCaseInsensitive() {
        var results = propertyRepository.searchProperties(null, "lyon", null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLocation()).isEqualTo("Lyon");
    }

    @Test
    void searchProperties_filtersByMaxPrice() {
        var results = propertyRepository.searchProperties(null, null, 700.0);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLocation()).isEqualTo("Marseille");
    }

    @Test
    void searchProperties_returnsAll_whenNoFilter() {
        assertThat(propertyRepository.searchProperties(null, null, null)).hasSize(3);
    }
}
