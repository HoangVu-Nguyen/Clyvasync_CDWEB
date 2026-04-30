package com.mediaservice.modules.photo.controller;

import com.commoncore.dto.response.ApiResponse;
import com.commonsecurity.secutiry.annotation.CurrentUserId;
import com.mediaservice.modules.photo.dto.request.BatchUploadRequest;
import com.mediaservice.modules.photo.dto.request.UploadRequest;
import com.mediaservice.modules.photo.dto.response.PresignedUrlResponse;
import com.mediaservice.modules.photo.service.IPhotoService;
import com.mediaservice.modules.photo.service.IS3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class PhotoController {
    private final IPhotoService photoService;


    @PostMapping("/presigned-url/batch")
    public ApiResponse<List<PresignedUrlResponse>> getBatchUploadUrls(
            @RequestBody @Valid BatchUploadRequest request,
            @CurrentUserId String userId) {
        return ApiResponse.success(photoService.prepareBatchUpload(userId, request));
    }
}
