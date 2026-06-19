package com.microservices.propertyservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.propertyservice.model.Property;
import com.microservices.propertyservice.model.Reservation;
import com.microservices.propertyservice.repository.PropertyRepository;
import com.microservices.propertyservice.repository.ReservationRepository;
import com.microservices.propertyservice.service.MinioService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PropertyController.class)
@AutoConfigureMockMvc(addFilters = false)
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PropertyRepository propertyRepository;

    @MockBean
    private ReservationRepository reservationRepository;

    @MockBean
    private MinioService minioService;

    private Property property(Long id) {
        Property p = new Property();
        p.setId(id);
        p.setTitle("Studio cosy");
        p.setType("APARTMENT");
        p.setLocation("Paris");
        p.setPrice(800.0);
        p.setOwnerId(7L);
        return p;
    }

    @Test
    void getAllProperties_returnsFindAll_whenNoFilter() throws Exception {
        when(propertyRepository.findAll()).thenReturn(List.of(property(1L)));

        mockMvc
            .perform(get("/properties"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Studio cosy"));

        verify(propertyRepository).findAll();
        verify(propertyRepository, never())
            .searchProperties(anyString(), anyString(), any());
    }

    @Test
    void getAllProperties_usesSearch_whenFilterPresent() throws Exception {
        when(propertyRepository.searchProperties(eq("APARTMENT"), eq(null), eq(null)))
            .thenReturn(List.of(property(1L)));

        mockMvc
            .perform(get("/properties").param("type", "APARTMENT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));

        verify(propertyRepository).searchProperties("APARTMENT", null, null);
    }

    @Test
    void getPropertyById_returnsProperty_whenFound() throws Exception {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property(1L)));

        mockMvc
            .perform(get("/properties/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.location").value("Paris"));
    }

    @Test
    void getPropertyById_returns404_whenMissing() throws Exception {
        when(propertyRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc
            .perform(get("/properties/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.service").value("property-service"));
    }

    @Test
    void createProperty_savesAndReturnsIt() throws Exception {
        Property toSave = property(null);
        when(propertyRepository.save(any(Property.class))).thenReturn(property(5L));

        mockMvc
            .perform(
                post("/properties")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(toSave))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void deleteProperty_returns404_whenMissing() throws Exception {
        when(propertyRepository.existsById(99L)).thenReturn(false);

        mockMvc
            .perform(delete("/properties/99"))
            .andExpect(status().isNotFound());

        verify(propertyRepository, never()).deleteById(99L);
    }

    @Test
    void deleteProperty_returns204_whenExists() throws Exception {
        when(propertyRepository.existsById(1L)).thenReturn(true);

        mockMvc
            .perform(delete("/properties/1"))
            .andExpect(status().isNoContent());

        verify(propertyRepository).deleteById(1L);
    }

    @Test
    void getReservedDates_returnsNonCancelledRanges() throws Exception {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property(1L)));
        Reservation r = new Reservation();
        r.setStartDate(LocalDate.of(2025, 7, 1));
        r.setEndDate(LocalDate.of(2025, 7, 5));
        when(reservationRepository.findByPropertyIdAndStatusNot(1L, "CANCELLED"))
            .thenReturn(List.of(r));

        mockMvc
            .perform(get("/properties/1/reserved-dates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].startDate").value("2025-07-01"))
            .andExpect(jsonPath("$[0].endDate").value("2025-07-05"));
    }

    @Test
    void generateUploadUrl_storesKeyAndReturnsUrl() throws Exception {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property(1L)));
        when(minioService.getUploadUrl(anyString()))
            .thenReturn("http://minio/upload");
        when(propertyRepository.save(any(Property.class))).thenReturn(property(1L));

        mockMvc
            .perform(post("/properties/1/photos/upload-url"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uploadUrl").value("http://minio/upload"))
            .andExpect(jsonPath("$.objectKey").isNotEmpty());

        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    void getPhotos_returnsDownloadUrlsForEachKey() throws Exception {
        Property p = property(1L);
        p.getPhotoKeys().add("key-1");
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(p));
        when(minioService.getDownloadUrl("key-1"))
            .thenReturn("http://minio/download/key-1");

        mockMvc
            .perform(get("/properties/1/photos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].objectKey").value("key-1"))
            .andExpect(jsonPath("$[0].downloadUrl").value("http://minio/download/key-1"));
    }

    @Test
    void health_returnsUp() throws Exception {
        mockMvc
            .perform(get("/properties/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("property-service"));
    }
}
