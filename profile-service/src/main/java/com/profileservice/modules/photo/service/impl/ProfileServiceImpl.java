package com.profileservice.modules.photo.service.impl;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.commoncore.dto.event.UserEventDTO;
import com.profileservice.modules.photo.constant.ImageConstants;
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
    @Override
    @Transactional(readOnly = true)
    @Cached(name = "userProfile:", key = "#userId", cacheType = CacheType.BOTH, expire = 1, timeUnit = TimeUnit.HOURS)
    public UserProfileResponse getUserProfile(Long userId) {
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

        // insertOrUpdate để chống trùng lặp dữ liệu nếu Kafka retry
        userInfoMapper.insert(userInfo);

        UserPhoto defaultAvatar = UserPhoto.builder()
                .userId(event.getUserId())
                .photoUrl(ImageConstants.AVATAR_DEFAULT)
                .photoType(ImageType.AVATAR)
                .isCurrent(true)
                .build();
        userPhotoMapper.insert(defaultAvatar);
    }
}
