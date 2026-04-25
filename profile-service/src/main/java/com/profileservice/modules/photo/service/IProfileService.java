package com.profileservice.modules.photo.service;

import com.commoncore.dto.event.UserEventDTO;
import com.profileservice.modules.photo.dto.response.UserHeaderResponse;
import com.profileservice.modules.photo.dto.response.UserProfileResponse;

public interface IProfileService {
    UserProfileResponse getUserProfile(String userId);
    void initUserProfile(UserEventDTO event);
    UserProfileResponse getProfileDetail(String ownerId, String viewerId);
    UserHeaderResponse getHeaderInfo(String  userId);
}
