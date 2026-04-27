package com.mediaservice.modules.photo.service.imp;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.commoncore.dto.event.MediaUpdateEvent;
import com.commoncore.enums.photo.ImageType;
import com.commoncore.enums.privacy.Privacy;
import com.mediaservice.modules.photo.entity.UserPhoto;
import com.mediaservice.modules.photo.mapper.UserPhotoMapper;
import com.mediaservice.modules.photo.service.IPhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class PhotoService implements IPhotoService {
    private final UserPhotoMapper userPhotoMapper;
    private final ApplicationEventPublisher eventPublisher;
    @Transactional(rollbackFor = Exception.class)
    public void uploadAndSetCurrentPhoto(String userId, String url, ImageType type) {
        log.info("Bắt đầu quy trình cập nhật ảnh {} cho user: {}", type, userId);

        LambdaUpdateWrapper<UserPhoto> retireOldPhotoWrapper = new LambdaUpdateWrapper<>();
        retireOldPhotoWrapper.eq(UserPhoto::getUserId, userId)
                .eq(UserPhoto::getPhotoType, type)
                .eq(UserPhoto::getIsCurrent, true)
                .set(UserPhoto::getIsCurrent, false);

        userPhotoMapper.update(null, retireOldPhotoWrapper);

        UserPhoto newPhoto = UserPhoto.builder()
                .userId(userId)
                .photoUrl(url)
                .photoType(type)
                .privacy(Privacy.PUBLIC) 
                .isCurrent(true)
                .createdAt(LocalDateTime.now())
                .build();

        userPhotoMapper.insert(newPhoto);
        log.info("Đã lưu thành công ảnh mới vào DB (Media Service)");

        MediaUpdateEvent event = new MediaUpdateEvent(
                userId,
                url,
                type,
                System.currentTimeMillis()
        );
        eventPublisher.publishEvent(event);

        log.info("Đã phát MediaUpdateEvent nội bộ, chờ Transaction Commit để bắn Kafka");
    }
}
