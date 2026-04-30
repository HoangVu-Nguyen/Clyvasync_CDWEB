package com.mediaservice.modules.photo.worker;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.commoncore.enums.status.PhotoStatus;
import com.mediaservice.modules.photo.entity.UserPhoto;
import com.mediaservice.modules.photo.mapper.UserPhotoMapper;
import com.mediaservice.modules.photo.service.IS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PhotoCleanupTask {
    private final UserPhotoMapper userPhotoMapper;
    private final IS3Service s3Service;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupPendingPhotos() {
        log.info(">>>> [CLEANUP] Bắt đầu quét ảnh PENDING hết hạn...");

        List<UserPhoto> pendingPhotos = userPhotoMapper.selectList(
                new LambdaQueryWrapper<UserPhoto>()
                        .eq(UserPhoto::getStatus, "PENDING")
                        .lt(UserPhoto::getCreatedAt, LocalDateTime.now().minusDays(1))
        );

        if (pendingPhotos.isEmpty()) {
            log.info(">>>> [CLEANUP] Không có ảnh nào cần xóa hôm nay.");
            return;
        }

        List<String> photoIds = pendingPhotos.stream()
                .map(UserPhoto::getId)
                .collect(Collectors.toList());

        List<String> objectKeys = pendingPhotos.stream()
                .map(UserPhoto::getPhotoUrl)
                .collect(Collectors.toList());

        try {
            s3Service.deleteFiles(objectKeys);

            userPhotoMapper.deleteBatchIds(photoIds);

            log.info(">>>> [CLEANUP] Dọn dẹp thành công {} ảnh rác.", photoIds.size());
        } catch (Exception e) {
            log.error(">>>> [CLEANUP] Quá trình dọn dẹp thất bại. Lỗi: {}", e.getMessage(), e);
        }
    }
}