package com.profileservice.modules.photo.handler;

import com.commoncore.dto.event.UserEventDTO;

public interface ProfileEventHandler {
    void handleRegistration(UserEventDTO payload);
}