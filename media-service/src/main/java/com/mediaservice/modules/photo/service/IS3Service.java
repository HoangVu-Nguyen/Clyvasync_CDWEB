package com.mediaservice.modules.photo.service;

import java.util.List;

public interface IS3Service {
    String generatePresignedPutUrl(String objectKey, String contentType, Long fileSize);
    void deleteFile(String objectKey);
     String getPublicUrl(String objectKey) ;
    void deleteFiles(List<String> objectKeys);
}
