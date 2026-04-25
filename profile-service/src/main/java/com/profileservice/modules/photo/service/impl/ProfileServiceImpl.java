package com.profileservice.modules.photo.service.impl;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.commoncore.dto.event.UserEventDTO;
import com.commoncore.enums.privacy.Privacy;
import com.commoncore.exception.AppException;
import com.commoncore.exception.ResultCode;
import com.commonlibrary.constant.SpiceDBConstants;
import com.commonlibrary.service.social.SpiceDbService;
import com.profileservice.modules.photo.constant.ImageConstants;
import com.profileservice.modules.photo.dto.response.UserHeaderResponse;
import com.profileservice.modules.photo.dto.response.UserPhotoResponse;
import com.profileservice.modules.photo.enums.ImageType;
import com.profileservice.modules.photo.dto.response.UserProfileResponse;
import com.profileservice.modules.photo.entity.profile.entity.UserInfo;
import com.profileservice.modules.photo.entity.profile.entity.UserPhoto;
import com.profileservice.modules.photo.mapper.UserInfoMapper;
import com.profileservice.modules.photo.mapper.UserPhotoMapper;
import com.profileservice.modules.photo.service.IProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor

public class ProfileServiceImpl implements IProfileService {
    private final UserInfoMapper userInfoMapper;
    private final UserPhotoMapper userPhotoMapper;
    private final SpiceDbService spiceDBService;
    @Override
    @Transactional(readOnly = true)
    @Cached(name = "userProfile:", key = "#userId", cacheType = CacheType.BOTH, expire = 1, timeUnit = TimeUnit.HOURS)
    public UserProfileResponse getUserProfile(String userId) {
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getUserId, userId);
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);

        String avatarUrl = ImageConstants.AVATAR_DEFAULT;
        String coverUrl = ImageConstants.COVER_DEFAULT;

        List<UserPhoto> photos = userPhotoMapper.findCurrentPhotos(userId);

        for (UserPhoto photo : photos) {
            if (photo.getPhotoType() == ImageType.AVATAR) {
                avatarUrl = photo.getPhotoUrl();
            } else if (photo.getPhotoType() == ImageType.COVER) {
                coverUrl = photo.getPhotoUrl();
            }
        }

        return UserProfileResponse.builder()
                .userId(userId)
                .bio(userInfo != null ? userInfo.getBio() : "")
                .location(userInfo != null ? userInfo.getLocation() : "")
                .birthDate(userInfo != null ? userInfo.getBirthDate() : null)
                .avatarUrl(avatarUrl)
                .coverUrl(coverUrl)
                .build();
    }
    @Override
    @Transactional
    public void initUserProfile(UserEventDTO event) {
        // 1. Tạo bản ghi Info
        UserInfo userInfo = UserInfo.builder()
                .userId(event.getUserId())
                .username(event.getUsername())
                .birthDate(event.getBirthDate())
                .bio("Chào mừng bạn đến với Clyvasync!")
                .build();

        userInfoMapper.insert(userInfo);

        UserPhoto defaultAvatar = UserPhoto.builder()
                .userId(event.getUserId())
                .photoUrl(ImageConstants.AVATAR_DEFAULT)
                .photoType(ImageType.AVATAR)
                .isCurrent(true)
                .build();
        userPhotoMapper.insert(defaultAvatar);
    }

    @Override
    @Transactional(readOnly = true)
    @Cached(name = "profileDetail:", key = "#ownerId + ':' + #viewerId", cacheType = CacheType.BOTH, expire = 1, timeUnit = TimeUnit.HOURS)
    public UserProfileResponse getProfileDetail(String ownerId, String viewerId) {
        UserInfo userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUserId, ownerId));
        if (userInfo == null) throw new AppException(ResultCode.USER_NOT_FOUND);

        boolean isOwner = ownerId.equals(viewerId);
        boolean canViewPrivate = false;

        if (isOwner) {
            canViewPrivate = true;
        } else {
            canViewPrivate = determineViewPermission(userInfo.getPrivacy(), ownerId, viewerId);
        }

        List<UserPhoto> currentPhotos = userPhotoMapper.selectList(new LambdaQueryWrapper<UserPhoto>()
                .eq(UserPhoto::getUserId, ownerId)
                .eq(UserPhoto::getIsCurrent, true));

        String avatarUrl = getPhotoByType(currentPhotos, ImageType.AVATAR, ImageConstants.AVATAR_DEFAULT);
        String coverUrl = getPhotoByType(currentPhotos, ImageType.COVER, ImageConstants.COVER_DEFAULT);

        return UserProfileResponse.builder()
                .userId(userInfo.getUserId())
                .username(userInfo.getUsername())
                .avatarUrl(avatarUrl)
                .coverUrl(coverUrl)
                .isOwner(isOwner)
                .canViewPrivateInfo(canViewPrivate)
                .bio(canViewPrivate ? userInfo.getBio() : "This profile is private")
                .location(canViewPrivate ? userInfo.getLocation() : "Hidden")
                .birthDate(canViewPrivate ? userInfo.getBirthDate() : null)
                .build();
    }

    @Cached(name = "userHeader", key = "#userId", cacheType = CacheType.BOTH, expire = 600)
    @Override
    public UserHeaderResponse getHeaderInfo(String userId) {
        UserInfo userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .select(UserInfo::getUsername)
                .eq(UserInfo::getUserId, userId));

        if (userInfo == null) throw new AppException(ResultCode.USER_NOT_FOUND);

        UserPhoto userPhoto = userPhotoMapper.selectOne(new LambdaQueryWrapper<UserPhoto>()
                .eq(UserPhoto::getUserId, userId)
                .eq(UserPhoto::getPhotoType, ImageType.AVATAR)
                .eq(UserPhoto::getIsCurrent, true));

        String finalPhotoUrl = (userPhoto != null) ? userPhoto.getPhotoUrl() : ImageConstants.AVATAR_DEFAULT;

        return UserHeaderResponse.builder()
                .id(userId)
                .username(userInfo.getUsername())
                .photoUrl(finalPhotoUrl)
                .build();
    }

    private boolean determineViewPermission(Privacy privacy, String ownerId, String viewerId) {
        if (privacy == null || privacy == Privacy.PUBLIC) return true;
        if (viewerId == null) return false;
        if (privacy == Privacy.PRIVATE) return false;

        if (privacy == Privacy.FRIENDS && viewerId != null) {
            return spiceDBService.checkPermission(
                    SpiceDBConstants.TargetType.USER, ownerId,
                    SpiceDBConstants.Permission.VIEW,
                    SpiceDBConstants.TargetType.USER, viewerId
            );
        }
        return false;
    }
    private String getPhotoByType(List<UserPhoto> photos, ImageType type, String defaultUrl) {
        return photos.stream()
                .filter(p -> type.equals(p.getPhotoType()))
                .map(UserPhoto::getPhotoUrl)
                .findFirst()
                .orElse(defaultUrl);
    }
}
