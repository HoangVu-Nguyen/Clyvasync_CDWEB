package com.mediaservice.modules.photo.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.commoncore.enums.photo.ImageType;
import com.mediaservice.modules.photo.dto.request.BatchUploadRequest;
import com.mediaservice.modules.photo.dto.request.UploadRequest;
import com.mediaservice.modules.photo.dto.response.PhotoResponse;
import com.mediaservice.modules.photo.dto.response.PresignedUrlResponse;
import com.mediaservice.modules.photo.entity.UserPhoto;

import java.util.List;

public interface IPhotoService extends IService<UserPhoto> {
    void uploadAndSetCurrentPhoto(String userId, String url, ImageType type);
    List<PresignedUrlResponse> prepareBatchUpload(String userId, BatchUploadRequest batchRequest);
    List<PhotoResponse> getLatestAuthorizedPhotos(String targetUserId, String currentUserId, int limit);
}
