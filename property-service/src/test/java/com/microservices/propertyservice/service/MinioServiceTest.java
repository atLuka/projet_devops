package com.microservices.propertyservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MinioServiceTest {

    private MinioService service;
    private MinioClient publicClient;

    @BeforeEach
    void setUp() {
        service = new MinioService();
        publicClient = mock(MinioClient.class);
        ReflectionTestUtils.setField(service, "publicClient", publicClient);
        ReflectionTestUtils.setField(service, "bucket", "properties");
    }

    @Test
    void getUploadUrl_returnsPresignedUrl() throws Exception {
        when(
            publicClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))
        ).thenReturn("http://minio/upload");

        assertThat(service.getUploadUrl("photo.jpg")).isEqualTo(
            "http://minio/upload"
        );
    }

    @Test
    void getUploadUrl_wrapsClientError() throws Exception {
        when(
            publicClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))
        ).thenThrow(new RuntimeException("minio down"));

        assertThatThrownBy(() -> service.getUploadUrl("photo.jpg"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("upload");
    }

    @Test
    void getDownloadUrl_returnsPresignedUrl() throws Exception {
        when(
            publicClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))
        ).thenReturn("http://minio/download");

        assertThat(service.getDownloadUrl("photo.jpg")).isEqualTo(
            "http://minio/download"
        );
    }

    @Test
    void getDownloadUrl_wrapsClientError() throws Exception {
        when(
            publicClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))
        ).thenThrow(new RuntimeException("minio down"));

        assertThatThrownBy(() -> service.getDownloadUrl("photo.jpg"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("téléchargement");
    }
}
