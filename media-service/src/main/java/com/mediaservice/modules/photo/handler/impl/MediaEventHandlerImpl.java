package com.mediaservice.modules.photo.handler.impl;

import com.commoncore.contanst.ImageConstants;
import com.commoncore.dto.event.UserEvent;
import com.commoncore.enums.photo.ImageType;
import com.mediaservice.modules.photo.entity.UserPhoto;
import com.mediaservice.modules.photo.mapper.UserPhotoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaEventHandlerImpl {
    private final UserPhotoMapper userPhotoMapper;

    @Transactional(rollbackFor = Exception.class)
    public void initializeUserPhotos(UserEvent payload) {
        String userId = payload.getUserId();

        // 1. Tạo bản ghi Avatar mặc định
        UserPhoto defaultAvatar = UserPhoto.builder()
                .userId(userId)
                .photoUrl(ImageConstants.AVATAR_DEFAULT)
                .photoType(ImageType.AVATAR)
                .isCurrent(true)
                .build();

        // 2. Tạo bản ghi Cover mặc định
        UserPhoto defaultCover = UserPhoto.builder()
                .userId(userId)
                .photoUrl(ImageConstants.COVER_DEFAULT)
                .photoType(ImageType.COVER)
                .isCurrent(true)
                .build();

        userPhotoMapper.insert(defaultAvatar);
        userPhotoMapper.insert(defaultCover);

        log.info(">>>> [DB MEDIA] Initialized default photos for user: {}", userId);
    }
}
