package com.profileservice.modules.photo.service;

import com.commoncore.dto.event.UserEventDTO;
import com.profileservice.modules.photo.dto.response.UserProfileResponse;

public interface IProfileService {
    UserProfileResponse getUserProfile(Long userId);
    void initUserProfile(UserEventDTO event);
}
