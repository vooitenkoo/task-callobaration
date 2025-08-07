package com.example.task_collaboration.domain.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class MinioService {

    private final MinioClient minioClient;
    @Value("${minio.bucket-name}")
    private String bucketName;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String uploadAvatar(MultipartFile file) throws Exception {
        // Проверяем, существует ли бакет
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        // Генерируем уникальное имя файла
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }

        // Возвращаем URL для доступа (можно использовать presigned URL для безопасности)
        return  "/" + bucketName + "/" + fileName;
    }

    public String uploadFile(MultipartFile file) throws Exception {
        // Проверяем, существует ли бакет
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        // Генерируем уникальное имя файла
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }

        // Возвращаем URL для доступа
        return "/" + bucketName + "/" + fileName;
    }
}