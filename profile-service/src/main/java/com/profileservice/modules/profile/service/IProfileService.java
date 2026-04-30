package com.profileservice.modules.profile.service;


import com.commoncore.dto.event.UserEvent;
import com.profileservice.modules.profile.dto.request.UpdateProfileRequest;
import com.profileservice.modules.profile.dto.response.UserHeaderResponse;
import com.profileservice.modules.profile.dto.response.UserProfileResponse;
import org.springframework.stereotype.Service;

public interface IProfileService {
    void initUserProfile(UserEvent event);
    UserProfileResponse getProfileDetail(String ownerId, String viewerId);
    UserHeaderResponse getHeaderInfo(String  userId);
    void updateProfile(String userId, UpdateProfileRequest request);
}
