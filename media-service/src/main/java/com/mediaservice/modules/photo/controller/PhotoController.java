package com.mediaservice.modules.photo.controller;

import com.commoncore.dto.response.ApiResponse;
import com.commonsecurity.secutiry.annotation.CurrentUserId;
import com.mediaservice.modules.photo.dto.request.UploadRequest;
import com.mediaservice.modules.photo.dto.response.PresignedUrlResponse;
import com.mediaservice.modules.photo.service.IS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class PhotoController {
    private final IS3Service s3Service;
    @PostMapping("/presigned-url")
    public ApiResponse<?> getUploadUrl(@RequestBody UploadRequest request, @CurrentUserId String userId) {

        // Tạo objectKey theo cấu trúc: users/userId/avatars/filename
        String objectKey = String.format("users/%s/%s/%s",
                userId, request.getImageType().name().toLowerCase(), request.getFileName());

        String uploadUrl = s3Service.generatePresignedPutUrl(objectKey, request.getContentType());

        // Trả về cho FE cả link upload và objectKey để sau này nó lưu vào Profile Service
        return  ApiResponse.success(new PresignedUrlResponse(uploadUrl, objectKey));
    }
}
