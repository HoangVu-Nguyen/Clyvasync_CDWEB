package com.profileservice.modules.profile.handler.impl;

import com.commoncore.dto.event.UserEvent;
import com.profileservice.modules.profile.handler.ProfileEventHandler;
import com.profileservice.modules.profile.service.IProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProfileRegistrationHandler implements ProfileEventHandler {

    private final IProfileService profileService;

    @Override
    public void handleRegistration(UserEvent payload) {
        if (payload.getUserId() == null) {
            log.error(">>>> [KAFKA] UserId is missing in payload!");
            return;
        }
        profileService.initUserProfile(payload);
    }
}