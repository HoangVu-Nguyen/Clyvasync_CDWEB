package com.profileservice.modules.photo.service.impl;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.commoncore.dto.event.UserEvent;
import com.commoncore.enums.privacy.Privacy;
import com.commoncore.exception.AppException;
import com.commoncore.exception.ResultCode;
import com.commonlibrary.constant.SpiceDBConstants;
import com.commonlibrary.service.social.SpiceDbService;
import com.profileservice.modules.photo.constant.ImageConstants;
import com.profileservice.modules.photo.dto.response.UserHeaderResponse;
import com.profileservice.modules.photo.dto.response.UserProfileResponse;
import com.profileservice.modules.photo.entity.profile.entity.UserInfo;
import com.profileservice.modules.photo.mapper.UserInfoMapper;
import com.profileservice.modules.photo.service.IProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor

public class ProfileServiceImpl implements IProfileService {
    private final UserInfoMapper userInfoMapper;
    private final SpiceDbService spiceDBService;
    @Override
    @Transactional(readOnly = true)
    @Cached(name = "userProfile:", key = "#userId", cacheType = CacheType.BOTH, expire = 1, timeUnit = TimeUnit.HOURS)
    public UserProfileResponse getUserProfile(String userId) {
        UserInfo userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUserId, userId));
        if (userInfo == null) {
            throw new AppException(ResultCode.USER_NOT_FOUND);
        }
        String avatarUrl = userInfo.getAvatarUrl() != null ? userInfo.getAvatarUrl() : ImageConstants.AVATAR_DEFAULT;
        String coverUrl = userInfo.getCoverUrl() != null ? userInfo.getCoverUrl() : ImageConstants.COVER_DEFAULT;

        return UserProfileResponse.builder()
                .userId(userId)
                .username(userInfo.getUsername())
                .bio(userInfo.getBio())
                .location(userInfo.getLocation())
                .birthDate(userInfo.getBirthDate())
                .avatarUrl(avatarUrl)
                .coverUrl(coverUrl)
                .build();
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
    }

    @Override
    @Transactional(readOnly = true)
    @Cached(name = "profileDetail:", key = "#ownerId + ':' + #viewerId", cacheType = CacheType.BOTH, expire = 1, timeUnit = TimeUnit.HOURS)
    public UserProfileResponse getProfileDetail(String ownerId, String viewerId) {
        UserInfo userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUserId, ownerId));

        if (userInfo == null) throw new AppException(ResultCode.USER_NOT_FOUND);

        boolean isOwner = ownerId.equals(viewerId);
        boolean canViewPrivate = isOwner || determineViewPermission(userInfo.getPrivacy(), ownerId, viewerId);

        return UserProfileResponse.builder()
                .userId(userInfo.getUserId())
                .username(userInfo.getUsername())
                .avatarUrl(userInfo.getAvatarUrl() != null ? userInfo.getAvatarUrl() : ImageConstants.AVATAR_DEFAULT)
                .coverUrl(userInfo.getCoverUrl() != null ? userInfo.getCoverUrl() : ImageConstants.COVER_DEFAULT)
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
                .select(UserInfo::getUsername, UserInfo::getAvatarUrl)
                .eq(UserInfo::getUserId, userId));

        if (userInfo == null) throw new AppException(ResultCode.USER_NOT_FOUND);

        return UserHeaderResponse.builder()
                .id(userId)
                .username(userInfo.getUsername())
                .photoUrl(userInfo.getAvatarUrl() != null ? userInfo.getAvatarUrl() : ImageConstants.AVATAR_DEFAULT)
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

}
