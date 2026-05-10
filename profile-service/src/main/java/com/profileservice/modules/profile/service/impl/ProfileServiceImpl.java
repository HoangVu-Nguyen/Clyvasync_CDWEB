package com.profileservice.modules.profile.service.impl;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.template.QuickConfig;
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
import com.commonlibrary.dto.record.SpiceDbRel;
import com.commonlibrary.service.social.SpiceDbService;

import com.profileservice.modules.profile.dto.event.ProfileMediaCommitEvent;
import com.profileservice.modules.profile.dto.request.UpdateProfileRequest;
import com.profileservice.modules.profile.dto.request.UserEducationRequest;
import com.profileservice.modules.profile.dto.response.UserEducationResponse;
import com.profileservice.modules.profile.dto.response.UserHeaderResponse;
import com.profileservice.modules.profile.dto.response.UserProfileResponse;
import com.profileservice.modules.profile.dto.response.UserWorkplaceResponse;
import com.profileservice.modules.profile.entity.profile.entity.UserEducation;
import com.profileservice.modules.profile.entity.profile.entity.UserInfo;
import com.profileservice.modules.profile.mapper.UserInfoMapper;
import com.profileservice.modules.profile.service.IProfileService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
    private final CacheManager cacheManager;


    private  Cache<String, UserHeaderResponse> headerCache;
    private  Cache<String, UserProfileResponse> profileCache;
    @PostConstruct
    public void init() {
        QuickConfig qcHeader = QuickConfig.newBuilder("userHeader")
                .expire(Duration.ofHours(1))
                .cacheType(CacheType.BOTH)
                .syncLocal(true)
                .build();
        headerCache = cacheManager.getOrCreateCache(qcHeader);

        QuickConfig qcProfile = QuickConfig.newBuilder("profileDetail:")
                .expire(Duration.ofHours(2))
                .cacheType(CacheType.BOTH)
                .syncLocal(true)
                .build();
        profileCache = cacheManager.getOrCreateCache(qcProfile);
    }

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
        try {
            List<SpiceDbRel> spiceDbRels = getSpiceDbRels(event);

            spiceDBService.writeRelationships(spiceDbRels);

            log.info("Successfully initialized SpiceDB permissions for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to write SpiceDB permission: {}", e.getMessage());
            throw new RuntimeException("SpiceDB integration failed, rolling back user creation");
        }
    }
    private static List<SpiceDbRel> getSpiceDbRels(UserEvent event) {
        List<SpiceDbRel> spiceDbRels = new ArrayList<>();
        spiceDbRels.add(new SpiceDbRel(SpiceDBConstants.TargetType.USER.name(), event.getUserId(), SpiceDBConstants.Relation.MANAGER.name(),
                SpiceDBConstants.TargetType.USER.name(), event.getUserId()));
        spiceDbRels.add(new SpiceDbRel(SpiceDBConstants.TargetType.RESOURCE.name(), event.getUserId(), SpiceDBConstants.Relation.OWNER.name(),
                SpiceDBConstants.TargetType.USER.name(), event.getUserId()));
        return spiceDbRels;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfileDetail(String ownerId, String viewerId) {
        UserInfo userInfo = getRawUserInfo(ownerId);
        boolean isOwner = ownerId.equals(viewerId);
        boolean isFriendCheck = false;

        if (!isOwner && viewerId != null) {
            try {
                isFriendCheck = spiceDBService.checkRelation(
                        SpiceDBConstants.TargetType.USER, ownerId,
                        SpiceDBConstants.Relation.FRIEND,
                        SpiceDBConstants.TargetType.USER, viewerId
                );
            } catch (Exception e) {
                log.error("SpiceDB check failed for owner {}: {}", ownerId, e.getMessage());
            }
        } else if (isOwner) {
            isFriendCheck = true;
        }

        final boolean finalIsFriend = isFriendCheck;

        boolean canViewBasic = canView(userInfo.getPrivacy(), isOwner, finalIsFriend);

        List<UserWorkplaceResponse> filteredWorkplaces;
        List<UserEducationResponse> filteredEducations;

        if (!canViewBasic) {
            filteredWorkplaces = List.of();
            filteredEducations = List.of();
        } else {
            filteredWorkplaces = workplaceService.getWorkplacesByUserId(ownerId)
                    .stream()
                    .filter(w -> canView(w.getPrivacy(), isOwner, finalIsFriend))
                    .toList();

            filteredEducations = educationService.getEducationsByUserId(ownerId)
                    .stream()
                    .filter(e -> canView(e.getPrivacy(), isOwner, finalIsFriend))
                    .toList();
        }

        return UserProfileResponse.builder()
                .userId(ownerId)
                .username(userInfo.getUsername())
                .avatarUrl(mediaUrlResolver.resolve(userInfo.getAvatarUrl() != null ? userInfo.getAvatarUrl() : ImageConstants.AVATAR_DEFAULT))
                .coverUrl(mediaUrlResolver.resolve(userInfo.getCoverUrl() != null ? userInfo.getCoverUrl() : ImageConstants.COVER_DEFAULT))
                .isOwner(isOwner)
                .privacy(userInfo.getPrivacy())
                .canViewPrivateInfo(canViewBasic)

                .website(canViewBasic ? userInfo.getWebsite() : null)
                .bio(canViewBasic ? userInfo.getBio() : "This profile is private")
                .location(canViewBasic ? userInfo.getLocation() : "Hidden")
                .birthDate(canViewBasic ? userInfo.getBirthDate() : null)
                .workplaces(filteredWorkplaces)
                .educations(filteredEducations)
                .build();
    }
    @Cached(name = "userHeader", key = "#userId", cacheType = CacheType.BOTH, expire = 600)
    @Override
    @Transactional(readOnly = true)
    public UserHeaderResponse getHeaderInfo(String userId) {
        UserInfo userInfo = getRawUserInfo(userId);

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
    @CacheInvalidate(name="headerCache", key = "#userId")
    @CacheInvalidate(name = "profileRawData:", key = "#userId")

    public void updateProfile(String userId, UpdateProfileRequest request) {
        boolean canEdit = spiceDBService.checkPermission(
                SpiceDBConstants.TargetType.RESOURCE, userId,
                SpiceDBConstants.Permission.EDIT,
                SpiceDBConstants.TargetType.USER, userId
        );
        if (!canEdit) {
            throw new AppException(ResultCode.ACCESS_DENIED);
        }
        UserInfo userInfo = userInfoMapper.selectById(userId);
        List<MediaUpdateEvent> mediaUpdateEvents = new ArrayList<>();

        boolean isChanged = false;


        if (isPhotoChanged(request.getAvatarUrl(), userInfo.getAvatarUrl())) {
            String newKey = extractKeyFromUrl(request.getAvatarUrl());
            userInfo.setAvatarUrl(newKey);
            mediaUpdateEvents.add(createMediaEvent(userId, newKey, ImageType.AVATAR));
            isChanged = true;
        }

        if (isPhotoChanged(request.getCoverUrl(), userInfo.getCoverUrl())) {
            String newKey = extractKeyFromUrl(request.getCoverUrl());
            userInfo.setCoverUrl(newKey);
            mediaUpdateEvents.add(createMediaEvent(userId, newKey, ImageType.COVER));
            isChanged = true;
        }

        isChanged |= updateFieldIfChanged(request.getBio(), userInfo::getBio, userInfo::setBio);
        isChanged |= updateFieldIfChanged(request.getLocation(), userInfo::getLocation, userInfo::setLocation);
        isChanged |= updateFieldIfChanged(request.getWebsite(), userInfo::getWebsite, userInfo::setWebsite);

        if (isChanged) {
            userInfoMapper.updateById(userInfo);
            log.info(">>>> [PROFILE] Profile updated for user: {}", userId);
        }
        if (request.getEducations() != null) {
            educationService.syncEducations(userId, request.getEducations());
        }

        if (request.getWorkplaces() != null) {
            workplaceService.syncWorkplaces(userId, request.getWorkplaces());
        }

        if (!mediaUpdateEvents.isEmpty()) {
            eventPublisher.publishEvent(new ProfileMediaCommitEvent(mediaUpdateEvents));
        }
    }
    @Cached(name = "profileRawData:", key = "#ownerId", cacheType = CacheType.BOTH, expire = 5, timeUnit = TimeUnit.MINUTES)
    public UserInfo getRawUserInfo(String ownerId) {
        UserInfo userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUserId, ownerId));
        if (userInfo == null) throw new AppException(ResultCode.USER_NOT_FOUND);
        return userInfo;
    }


    private boolean updateFieldIfChanged(String newValue, Supplier<String> getter, Consumer<String> setter) {
        if (newValue != null && !Objects.equals(newValue, getter.get())) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }



    private MediaUpdateEvent createMediaEvent(String userId, String url, ImageType type) {
        return MediaUpdateEvent.builder().userId(userId).url(url).type(type).build();
    }
    private boolean isPhotoChanged(String requestUrl, String currentKeyInDb) {
        if (requestUrl == null) return false;
        String extractedKey = extractKeyFromUrl(requestUrl);

        return !Objects.equals(extractedKey, currentKeyInDb);
    }

    private String extractKeyFromUrl(String url) {
        if (url == null) return null;
        if (url.contains("users/")) {
            return url.substring(url.indexOf("users/"));
        }
        return url;
    }

}
