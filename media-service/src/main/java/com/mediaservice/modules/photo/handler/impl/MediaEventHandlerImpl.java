package com.mediaservice.modules.photo.handler.impl;

import com.commoncore.contanst.ImageConstants;
import com.commoncore.dto.event.MediaUpdateEvent;
import com.commoncore.dto.event.UserEvent;
import com.commoncore.enums.photo.ImageType;
import com.commoncore.enums.status.PhotoStatus;
import com.mediaservice.modules.photo.entity.UserPhoto;
import com.mediaservice.modules.photo.handler.MediaEventHandler;
import com.mediaservice.modules.photo.mapper.UserPhotoMapper;
import com.mediaservice.modules.photo.service.IPhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaEventHandlerImpl implements MediaEventHandler {
    private final UserPhotoMapper userPhotoMapper;
    private final IPhotoService photoService;

    @Transactional(rollbackFor = Exception.class)
    public void initializeUserPhotos(UserEvent payload) {
        String userId = payload.getUserId();

        // 1. Tạo bản ghi Avatar mặc định
        UserPhoto defaultAvatar = UserPhoto.builder()
                .userId(userId)
                .photoUrl(ImageConstants.AVATAR_DEFAULT)
                .photoType(ImageType.AVATAR)
                .status(PhotoStatus.ACTIVE)
                .isCurrent(true)
                .build();

        // 2. Tạo bản ghi Cover mặc định
        UserPhoto defaultCover = UserPhoto.builder()
                .userId(userId)
                .photoUrl(ImageConstants.COVER_DEFAULT)
                .photoType(ImageType.COVER)
                .status(PhotoStatus.ACTIVE)
                .isCurrent(true)
                .build();

        userPhotoMapper.insert(defaultAvatar);
        userPhotoMapper.insert(defaultCover);

        log.info(">>>> [DB MEDIA] Initialized default photos for user: {}", userId);
    }

    @Override
    @Transactional
    public void handleMediaConfirm(MediaUpdateEvent event) {
        String userId = event.userId();
        String objectKey = event.url();
        ImageType type = event.type();

       photoService.uploadAndSetCurrentPhoto(userId, objectKey, type);

    }
}
