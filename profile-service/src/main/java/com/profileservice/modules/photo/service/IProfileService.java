package com.profileservice.modules.photo.service;

import com.commoncore.dto.event.UserEvent;
import com.profileservice.modules.photo.dto.response.UserHeaderResponse;
import com.profileservice.modules.photo.dto.response.UserProfileResponse;

public interface IProfileService {
    UserProfileResponse getUserProfile(String userId);
    void initUserProfile(UserEvent event);
    UserProfileResponse getProfileDetail(String ownerId, String viewerId);
    UserHeaderResponse getHeaderInfo(String  userId);
}
