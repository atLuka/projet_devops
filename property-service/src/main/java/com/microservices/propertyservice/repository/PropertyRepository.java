package com.microservices.propertyservice.repository;

import com.microservices.propertyservice.model.Property;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByOwnerId(Long ownerId);

    List<Property> findByType(String type);

    List<Property> findByLocationContainingIgnoreCase(String location);

    List<Property> findByPriceLessThanEqual(Double maxPrice);

    @Query(
        value = "SELECT * FROM properties WHERE " +
            "(CAST(:type AS VARCHAR) IS NULL OR type = CAST(:type AS VARCHAR)) AND " +
            "(CAST(:location AS VARCHAR) IS NULL OR LOWER(location) LIKE LOWER(CONCAT('%', CAST(:location AS VARCHAR), '%'))) AND " +
            "(:maxPrice IS NULL OR price <= :maxPrice)",
        nativeQuery = true
    )
    List<Property> searchProperties(
        @Param("type") String type,
        @Param("location") String location,
        @Param("maxPrice") Double maxPrice
    );
}
