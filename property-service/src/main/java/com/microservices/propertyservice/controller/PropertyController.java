package com.microservices.propertyservice.controller;

import com.microservices.propertyservice.exception.EntityNotFoundException;
import com.microservices.propertyservice.model.Property;
import com.microservices.propertyservice.model.Reservation;
import com.microservices.propertyservice.repository.PropertyRepository;
import com.microservices.propertyservice.repository.ReservationRepository;
import com.microservices.propertyservice.service.MinioService;
import java.time.LocalDate;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/properties")
public class PropertyController {

    private static final Logger log = LoggerFactory.getLogger(
        PropertyController.class
    );

    private final PropertyRepository propertyRepository;
    private final ReservationRepository reservationRepository;
    private final MinioService minioService;

    public PropertyController(
        PropertyRepository propertyRepository,
        ReservationRepository reservationRepository,
        MinioService minioService
    ) {
        this.propertyRepository = propertyRepository;
        this.reservationRepository = reservationRepository;
        this.minioService = minioService;
    }

    @GetMapping
    public List<Property> getAllProperties(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String location,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate endDate
    ) {
        log.info(
            "GET /properties - type={}, location={}, maxPrice={}, startDate={}, endDate={}",
            type,
            location,
            maxPrice,
            startDate,
            endDate
        );

        List<Property> properties;
        if (type == null && location == null && maxPrice == null) {
            properties = propertyRepository.findAll();
        } else {
            properties = propertyRepository.searchProperties(
                type,
                location,
                maxPrice
            );
        }

        if (startDate != null && endDate != null) {
            properties = properties
                .stream()
                .filter(p ->
                    reservationRepository
                        .findOverlapping(p.getId(), startDate, endDate)
                        .isEmpty()
                )
                .toList();
        }

        return properties;
    }

    @GetMapping("/{id}")
    public Property getPropertyById(@PathVariable Long id) {
        log.info("GET /properties/{}", id);
        return propertyRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Logement non trouvé : " + id)
            );
    }

    @PostMapping
    public Property createProperty(@RequestBody Property property) {
        log.info("POST /properties - title={}", property.getTitle());
        return propertyRepository.save(property);
    }

    @PutMapping("/{id}")
    public Property updateProperty(
        @PathVariable Long id,
        @RequestBody Property property
    ) {
        log.info("PUT /properties/{}", id);
        Property existing = propertyRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Logement non trouvé : " + id)
            );
        existing.setTitle(property.getTitle());
        existing.setType(property.getType());
        existing.setLocation(property.getLocation());
        existing.setPrice(property.getPrice());
        existing.setDescription(property.getDescription());
        existing.setOwnerId(property.getOwnerId());
        return propertyRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        log.info("DELETE /properties/{}", id);
        if (!propertyRepository.existsById(id)) {
            throw new EntityNotFoundException("Logement non trouvé : " + id);
        }
        propertyRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/owner/{ownerId}")
    public List<Property> getPropertiesByOwner(@PathVariable Long ownerId) {
        log.info("GET /properties/owner/{}", ownerId);
        return propertyRepository.findByOwnerId(ownerId);
    }

    @GetMapping("/{id}/reserved-dates")
    public List<Map<String, String>> getReservedDates(@PathVariable Long id) {
        log.info("GET /properties/{}/reserved-dates", id);
        propertyRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Logement non trouvé : " + id)
            );
        List<Reservation> reservations =
            reservationRepository.findByPropertyIdAndStatusNot(id, "CANCELLED");
        return reservations
            .stream()
            .map(r ->
                Map.of(
                    "startDate",
                    r.getStartDate().toString(),
                    "endDate",
                    r.getEndDate().toString()
                )
            )
            .toList();
    }

    @PostMapping("/{id}/photos/upload-url")
    public Map<String, String> generateUploadUrl(@PathVariable Long id) {
        log.info("POST /properties/{}/photos/upload-url", id);
        Property property = propertyRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Logement non trouvé : " + id)
            );
        String objectKey = UUID.randomUUID().toString();
        String uploadUrl = minioService.getUploadUrl(objectKey);
        property.getPhotoKeys().add(objectKey);
        propertyRepository.save(property);
        return Map.of("uploadUrl", uploadUrl, "objectKey", objectKey);
    }

    @DeleteMapping("/{id}/photos/{objectKey}")
    public ResponseEntity<Void> deletePhoto(
        @PathVariable Long id,
        @PathVariable String objectKey
    ) {
        log.info("DELETE /properties/{}/photos/{}", id, objectKey);
        Property property = propertyRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Logement non trouvé : " + id)
            );
        property.getPhotoKeys().remove(objectKey);
        propertyRepository.save(property);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public java.util.Map<String, String> health() {
        return java.util.Map.of("status", "UP", "service", "property-service");
    }

    @GetMapping("/{id}/photos")
    public List<Map<String, String>> getPhotos(@PathVariable Long id) {
        log.info("GET /properties/{}/photos", id);
        Property property = propertyRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Logement non trouvé : " + id)
            );
        List<Map<String, String>> photos = new ArrayList<>();
        for (String objectKey : property.getPhotoKeys()) {
            Map<String, String> photo = new HashMap<>();
            photo.put("objectKey", objectKey);
            photo.put("downloadUrl", minioService.getDownloadUrl(objectKey));
            photos.add(photo);
        }
        return photos;
    }
}
