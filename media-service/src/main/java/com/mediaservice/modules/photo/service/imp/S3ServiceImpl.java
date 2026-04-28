package com.mediaservice.modules.photo.service.imp;

import com.mediaservice.modules.photo.service.IS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
@RequiredArgsConstructor
@Service
public class S3ServiceImpl implements IS3Service {
    private final S3Presigner s3Presigner;
    @Value("${aws.s3.bucket-name}") private String bucketName;

    @Override
    public String generatePresignedPutUrl(String objectKey, String contentType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(objectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    @Override
    public void deleteFile(String objectKey) {

    }
}
