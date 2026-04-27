package com.mediaservice.modules.photo.service;


import com.commoncore.enums.photo.ImageType;

public interface IPhotoService {
    void uploadAndSetCurrentPhoto(String userId, String url, ImageType type);
}
