package com.mediaservice.modules.photo.controller;

import com.commoncore.dto.response.ApiResponse;
import com.commonsecurity.secutiry.annotation.CurrentUserId;
import com.mediaservice.modules.photo.dto.request.BatchUploadRequest;
import com.mediaservice.modules.photo.dto.request.UploadRequest;
import com.mediaservice.modules.photo.dto.response.PhotoResponse;
import com.mediaservice.modules.photo.dto.response.PresignedUrlResponse;
import com.mediaservice.modules.photo.service.IPhotoService;
import com.mediaservice.modules.photo.service.IS3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/{ownerId}/latest")
    public ApiResponse<List<PhotoResponse>> getLatestAuthorizedPhotos(
            @PathVariable("ownerId") String ownerId,
            @CurrentUserId String currentUserId,
            @RequestParam(defaultValue = "6") int limit
    )
    {
        return ApiResponse.success(photoService.getLatestAuthorizedPhotos(ownerId, currentUserId, limit));
    }


}
