package com.mediaservice.modules.photo.service;

public interface IS3Service {
    String generatePresignedPutUrl(String objectKey, String contentType, Long fileSize);
    void deleteFile(String objectKey);
}
