package com.profileservice.modules.profile.service.impl;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.commoncore.contanst.ImageConstants;
import com.commoncore.contanst.KafkaConstant;
import com.commoncore.dto.event.MediaUpdateEvent;
import com.commoncore.dto.event.UserEvent;
import com.commoncore.enums.photo.ImageType;
import com.commoncore.enums.privacy.Privacy;
import com.commoncore.exception.AppException;
import com.commoncore.exception.ResultCode;
import com.commoncore.producer.CoreKafkaProducer;
import com.commoncore.resolver.MediaUrlResolver;
import com.commonlibrary.constant.SpiceDBConstants;
import com.commonlibrary.service.social.SpiceDbService;

import com.profileservice.modules.profile.dto.event.ProfileMediaCommitEvent;
import com.profileservice.modules.profile.dto.request.UpdateProfileRequest;
import com.profileservice.modules.profile.dto.response.UserEducationResponse;
import com.profileservice.modules.profile.dto.response.UserHeaderResponse;
import com.profileservice.modules.profile.dto.response.UserProfileResponse;
import com.profileservice.modules.profile.dto.response.UserWorkplaceResponse;
import com.profileservice.modules.profile.entity.profile.entity.UserInfo;
import com.profileservice.modules.profile.mapper.UserInfoMapper;
import com.profileservice.modules.profile.service.IProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements IProfileService {
    private final UserInfoMapper userInfoMapper;
    private final SpiceDbService spiceDBService;
    private final EducationService educationService;
    private final WorkplaceService workplaceService;
    private final MediaUrlResolver mediaUrlResolver;
    private final ApplicationEventPublisher eventPublisher;


    @Override
    @Transactional
    public void initUserProfile(UserEvent event) {
        UserInfo userInfo = UserInfo.builder()
                .userId(event.getUserId())
                .username(event.getUsername())
                .birthDate(event.getBirthDate())
                .bio("Chào mừng bạn đến với Clyvasync!")
                .avatarUrl(ImageConstants.AVATAR_DEFAULT)
                .coverUrl(ImageConstants.COVER_DEFAULT)
                .build();

        userInfoMapper.insert(userInfo);
    }

    @Override
    @Transactional(readOnly = true)
    @Cached(name = "profileDetail:", key = "#ownerId + ':' + #viewerId", cacheType = CacheType.BOTH, expire = 1, timeUnit = TimeUnit.HOURS)
    public UserProfileResponse getProfileDetail(String ownerId, String viewerId) {
        // 1. Query Info cơ bản
        UserInfo userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUserId, ownerId));
        if (userInfo == null) throw new AppException(ResultCode.USER_NOT_FOUND);

        boolean isOwner = ownerId.equals(viewerId);

        boolean isFriendCheck = false;
        if (!isOwner && viewerId != null) {
            try {
                isFriendCheck = spiceDBService.checkPermission(
                        SpiceDBConstants.TargetType.USER, viewerId,
                        SpiceDBConstants.Permission.VIEW,
                        SpiceDBConstants.TargetType.USER, ownerId
                );
            } catch (Exception e) {
                log.error("SpiceDB check failed for owner {}: {}", ownerId, e.getMessage());
            }
        }
        final boolean finalIsFriend = isFriendCheck;

        // 3. Lấy dữ liệu thô và Filter bằng logic canView (In-memory)
        List<UserWorkplaceResponse> filteredWorkplaces = workplaceService.getWorkplacesByUserId(ownerId)
                .stream()
                .filter(w -> canView(w.getPrivacy(), isOwner, finalIsFriend))
                .toList();

        List<UserEducationResponse> filteredEducations = educationService.getEducationsByUserId(ownerId)
                .stream()
                .filter(e -> canView(e.getPrivacy(), isOwner, finalIsFriend))
                .toList();

        boolean canViewBasic = canView(userInfo.getPrivacy(), isOwner, finalIsFriend);

        return UserProfileResponse.builder()
                .userId(ownerId)
                .username(userInfo.getUsername())
                .avatarUrl(mediaUrlResolver.resolve(userInfo.getAvatarUrl() != null ? userInfo.getAvatarUrl() : ImageConstants.AVATAR_DEFAULT))
                .coverUrl(mediaUrlResolver.resolve(userInfo.getCoverUrl() != null ? userInfo.getCoverUrl() : ImageConstants.COVER_DEFAULT))
                .isOwner(isOwner)
                .canViewPrivateInfo(canViewBasic)
                .privacy(userInfo.getPrivacy())
                .bio(canViewBasic ? userInfo.getBio() : "This profile is private")
                .location(canViewBasic ? userInfo.getLocation() : "Hidden")
                .birthDate(canViewBasic ? userInfo.getBirthDate() : null)
                .workplaces(filteredWorkplaces)
                .educations(filteredEducations)
                .build();
    }

    @Cached(name = "userHeader", key = "#userId", cacheType = CacheType.BOTH, expire = 600)
    @Override
    public UserHeaderResponse getHeaderInfo(String userId) {
        UserInfo userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .select(UserInfo::getUsername, UserInfo::getAvatarUrl)
                .eq(UserInfo::getUserId, userId));

        if (userInfo == null) throw new AppException(ResultCode.USER_NOT_FOUND);

        return UserHeaderResponse.builder()
                .id(userId)
                .username(userInfo.getUsername())
                .photoUrl(userInfo.getAvatarUrl() != null ? userInfo.getAvatarUrl() : ImageConstants.AVATAR_DEFAULT)
                .build();
    }

    private boolean canView(Privacy targetPrivacy, boolean isOwner, boolean isFriend) {
        if (isOwner) return true;
        if (targetPrivacy == Privacy.PUBLIC) return true;
        if (targetPrivacy == Privacy.FRIENDS) return isFriend;
        return false;
    }
    @Override
    @Transactional
    public void updateProfile(String userId, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", request);
        UserInfo userInfo = userInfoMapper.selectById(userId);
        List<MediaUpdateEvent> mediaUpdateEvents = new ArrayList<>();

        // 1. Xử lý Avatar
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().equals(userInfo.getAvatarUrl())) {
            userInfo.setAvatarUrl(request.getAvatarUrl());
            mediaUpdateEvents.add(MediaUpdateEvent.builder()
                    .userId(userId)
                    .url(request.getAvatarUrl())
                    .type(ImageType.AVATAR)
                    .build());
        }

        // 2. Xử lý Cover
        if (request.getCoverUrl() != null && !request.getCoverUrl().equals(userInfo.getCoverUrl())) {
            // Đã fix lỗi copy nhầm getAvatarUrl thành getCoverUrl cho Vũ
            mediaUpdateEvents.add(MediaUpdateEvent.builder()
                    .userId(userId)
                    .url(request.getCoverUrl())
                    .type(ImageType.COVER)
                    .build());
            userInfo.setCoverUrl(request.getCoverUrl());
        }

        userInfo.setBio(request.getBio());

        // 3. Thực hiện Update DB
        userInfoMapper.updateById(userInfo);

        // 4. Nếu có ảnh mới, phát sự kiện NỘI BỘ chờ Commit
        if (!mediaUpdateEvents.isEmpty()) {
            eventPublisher.publishEvent(new ProfileMediaCommitEvent(mediaUpdateEvents));
        }
    }
}
