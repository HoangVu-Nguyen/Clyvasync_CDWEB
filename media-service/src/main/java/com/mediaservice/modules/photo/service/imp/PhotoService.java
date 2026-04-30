package com.mediaservice.modules.photo.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.commoncore.dto.event.MediaUpdateEvent;
import com.commoncore.enums.photo.ImageType;
import com.commoncore.enums.privacy.Privacy;
import com.commoncore.enums.status.PhotoStatus;
import com.commoncore.exception.AppException;
import com.commoncore.exception.ResultCode;
import com.commonlibrary.constant.SpiceDBConstants;
import com.commonlibrary.service.social.SpiceDbService;
import com.mediaservice.modules.photo.dto.request.BatchUploadRequest;
import com.mediaservice.modules.photo.dto.request.UploadRequest;
import com.mediaservice.modules.photo.dto.response.PhotoResponse;
import com.mediaservice.modules.photo.dto.response.PresignedUrlResponse;
import com.mediaservice.modules.photo.entity.UserPhoto;
import com.mediaservice.modules.photo.mapper.UserPhotoMapper;
import com.mediaservice.modules.photo.mapstruct.PhotoMapper;
import com.mediaservice.modules.photo.service.IPhotoService;
import com.mediaservice.modules.photo.service.IS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class PhotoService extends ServiceImpl<UserPhotoMapper,UserPhoto> implements IPhotoService {
    private final UserPhotoMapper userPhotoMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final IS3Service s3Service;
    private final SpiceDbService spiceDbService;
    private final PhotoMapper photoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadAndSetCurrentPhoto(String userId, String objectKey, ImageType type) {
        log.info("Bắt đầu xác nhận ảnh (Confirm) {} cho user: {}", type, userId);

        LambdaUpdateWrapper<UserPhoto> retireWrapper = new LambdaUpdateWrapper<>();
        retireWrapper.eq(UserPhoto::getUserId, userId)
                .eq(UserPhoto::getPhotoType, type)
                .eq(UserPhoto::getIsCurrent, true)
                .set(UserPhoto::getIsCurrent, false);
        userPhotoMapper.update(null, retireWrapper);


        LambdaUpdateWrapper<UserPhoto> confirmWrapper = new LambdaUpdateWrapper<>();
        confirmWrapper.eq(UserPhoto::getUserId, userId)
                .eq(UserPhoto::getPhotoUrl, objectKey)
                .eq(UserPhoto::getStatus, PhotoStatus.PENDING)
                .set(UserPhoto::getPhotoUrl, objectKey)
                .set(UserPhoto::getStatus, PhotoStatus.ACTIVE)
                .set(UserPhoto::getIsCurrent, true);

        int updated = userPhotoMapper.update(null, confirmWrapper);

        if (updated == 0) {
            log.error("Không tìm thấy bản ghi PENDING để confirm cho Key: {}", objectKey);
            throw new AppException(ResultCode.UPLOAD_FAILED);
        }

        MediaUpdateEvent event = new MediaUpdateEvent(
                userId,
                objectKey,
                type,
                System.currentTimeMillis()
        );
        //   eventPublisher.publishEvent(event);

        log.info("Xác nhận ảnh thành công, trạng thái đã chuyển sang ACTIVE");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PresignedUrlResponse> prepareBatchUpload(String userId, BatchUploadRequest batchRequest) {

        List<UploadRequest> items = batchRequest.getItems();
        if (items == null || items.isEmpty()) {
            log.warn("Batch upload request is empty for user: {}", userId);
            return Collections.emptyList();
        }
        log.info("Bắt đầu khởi tạo lô upload gồm {} file cho user: {}", items.size(), userId);
        List<PresignedUrlResponse> responses = new ArrayList<>(items.size());
        List<UserPhoto> pendingPhotos = new ArrayList<>(items.size());

        for (UploadRequest request : items) {
            String objectKey = generateObjectKey(userId, request);
            String uploadUrl = s3Service.generatePresignedPutUrl(
                    objectKey,
                    request.getContentType(),
                    request.getFileSize()
            );
            UserPhoto photo = UserPhoto.builder()
                    .userId(userId)
                    .photoUrl(objectKey)
                    .photoType(request.getImageType())
                    .status(PhotoStatus.PENDING)
                    .isCurrent(false)
                    .build();
            pendingPhotos.add(photo);
            responses.add(new PresignedUrlResponse(uploadUrl, objectKey));
        }
        boolean isSaved = this.saveBatch(pendingPhotos, 100);
        if (!isSaved) {
            log.error("Lỗi khi lưu bảng tạm (Batch Insert) cho user: {}", userId);
            throw new RuntimeException("Không thể khởi tạo phiên upload. Vui lòng thử lại!");
        }
        log.info("Khởi tạo lô upload thành công cho user: {}", userId);
        return responses;
    }


    private String generateObjectKey(String userId, UploadRequest request) {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String safeFileName = request.getFileName().replaceAll("[^a-zA-Z0-9.-]", "_");
        return String.format("users/%s/%s/%s-%s",
                userId,
                request.getImageType().name().toLowerCase(),
                uniqueSuffix,
                safeFileName);
    }

    @Override
    @Cacheable(value = "latest_authorized_photos", key = "#targetUserId + ':' + #currentUserId + ':' + #limit")
    @Transactional(readOnly = true)
    public List<PhotoResponse> getLatestAuthorizedPhotos(String targetUserId, String currentUserId, int limit) {
        List<UserPhoto> authorizedPhotos = new ArrayList<>();
        boolean isOwner = targetUserId.equals(currentUserId);
        int pageNum = 1;
        int pageSize = 15;
        while (authorizedPhotos.size() < limit) {
            Page<UserPhoto> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<UserPhoto> queryWrapper = new LambdaQueryWrapper<UserPhoto>()
                    .eq(UserPhoto::getUserId, targetUserId)
                    .eq(UserPhoto::getStatus, PhotoStatus.ACTIVE)
                    .orderByDesc(UserPhoto::getCreatedAt);

            Page<UserPhoto> photoPage = this.page(page, queryWrapper);
            List<UserPhoto> records = photoPage.getRecords();

            if (records.isEmpty()) {
                break;
            }

            for (UserPhoto photo : records) {
                if (isOwner || hasPermissionToView(currentUserId, targetUserId, photo)) {
                    authorizedPhotos.add(photo);

                    if (authorizedPhotos.size() == limit) {
                        return photoMapper.toPhotoResponseList(authorizedPhotos);
                    }
                }
            }
            pageNum++;
        }
        return photoMapper.toPhotoResponseList(authorizedPhotos);
    }

    private boolean hasPermissionToView(String currentUserId, String targetUserId, UserPhoto photo) {
        Privacy privacy = photo.getPrivacy();

        if (Privacy.PUBLIC.equals(privacy)) {
            return true;
        }
        if (Privacy.PRIVATE.equals(privacy)){
            return false;
        }

        if (Privacy.FRIENDS.equals(privacy)) {
            return spiceDbService.checkPermission(
                    SpiceDBConstants.TargetType.USER, targetUserId,
                    SpiceDBConstants.Permission.VIEW,
                    SpiceDBConstants.TargetType.RESOURCE, currentUserId
            );
        }
        return false;
    }

}
