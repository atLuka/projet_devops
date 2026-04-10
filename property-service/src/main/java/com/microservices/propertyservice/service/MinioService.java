package com.microservices.propertyservice.service;

import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MinioService {

    @Value("${minio.url}")
    private String internalUrl;

    @Value("${minio.public-url}")
    private String publicUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucket;

    private MinioClient internalClient;
    private MinioClient publicClient;

    @PostConstruct
    public void init() throws Exception {
        internalClient = MinioClient.builder()
            .endpoint(internalUrl)
            .credentials(accessKey, secretKey)
            .build();

        publicClient = MinioClient.builder()
            .endpoint(publicUrl)
            .credentials(accessKey, secretKey)
            .region("us-east-1")
            .build();

        boolean bucketExists = internalClient.bucketExists(
            BucketExistsArgs.builder().bucket(bucket).build()
        );
        if (!bucketExists) {
            internalClient.makeBucket(
                MakeBucketArgs.builder().bucket(bucket).build()
            );
        }
    }

    public String getUploadUrl(String objectKey) {
        try {
            return publicClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(15, TimeUnit.MINUTES)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(
                "Erreur MinIO lors de la génération de l'URL d'upload",
                e
            );
        }
    }

    public String getDownloadUrl(String objectKey) {
        try {
            return publicClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(1, TimeUnit.HOURS)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(
                "Erreur MinIO lors de la génération de l'URL de téléchargement",
                e
            );
        }
    }
}
